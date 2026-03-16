package com.fruits.tape

import com.fruits.domain.model.image.DownloadUrlData
import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.usecase.interactions.GetIncomingLikesUseCase
import com.fruits.domain.usecase.interactions.SendUserActionUseCase
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import com.fruits.logger.Log
import com.fruits.tape.components.swipe_card.SwipeDirection
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TapeViewModelTest {

    @MockK
    private lateinit var getRecommendationsUseCase: GetRecommendationsUseCase
    @MockK
    private lateinit var getIncomingLikesUseCase: GetIncomingLikesUseCase
    @MockK
    private lateinit var sendUserActionUseCase: SendUserActionUseCase
    @MockK
    private lateinit var imageUploadUrlRepository: ImageUploadUrlRepository

    private lateinit var viewModel: TapeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkObject(Log)
        every { Log.v(any(), any()) } returns Unit
        every { Log.d(any(), any()) } returns Unit
        every { Log.i(any(), any()) } returns Unit
        every { Log.w(any(), any()) } returns Unit
        every { Log.e(any(), any()) } returns Unit
        every { Log.e(any(), any(), any()) } returns Unit

        coEvery { getRecommendationsUseCase() } returns Result.success(emptyList())
        coEvery { getIncomingLikesUseCase() } returns Result.success(emptyList())

        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Recommendations Tests ---

    @Test
    fun `initialization loads recommendations and sets current card`() = runTest {
        val recommendation = Recommendations(
            userId = "rec_1",
            firstName = "John",
            secondName = "Doe",
            age = 25,
            city = "Moscow",
            photoFileKeys = listOf("key1"),
            description = "About me",
            explanation = listOf("Reason 1")
        )
        val downloadUrl = DownloadUrlData(key = "key1", url = "http://image.com/1")

        coEvery { getRecommendationsUseCase() } returns Result.success(listOf(recommendation))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(listOf(downloadUrl))

        // Re-init to trigger load in init block
        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("rec_1", state.currentCard?.userId)
        assertEquals("http://image.com/1", state.currentCard?.imageUrl)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `handleSwipe RIGHT on recommendation sends LIKE action and moves to next`() = runTest {
        val rec1 = createRecommendation("1")
        val rec2 = createRecommendation("2")
        coEvery { getRecommendationsUseCase() } returns Result.success(listOf(rec1, rec2))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())
        coEvery { sendUserActionUseCase(any(), any()) } returns Result.success(Unit)

        viewModel = TapeViewModel(getRecommendationsUseCase, getIncomingLikesUseCase, sendUserActionUseCase, imageUploadUrlRepository)
        advanceUntilIdle()

        viewModel.onEvent(TapeEvent.OnCardSwiped(SwipeDirection.RIGHT))
        advanceUntilIdle()

        coVerify { sendUserActionUseCase("1", UserAction.LIKE) }
        assertEquals("2", viewModel.state.value.currentCard?.userId)
    }

    // --- Liked Tab Tests ---

    @Test
    fun `onTabSelected Liked loads incoming likes only once`() = runTest {
        val like = createIncomingLike("user_liked")
        coEvery { getIncomingLikesUseCase() } returns Result.success(listOf(like))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())

        // Select Liked tab
        viewModel.onEvent(TapeEvent.OnTabSelected(TapeTab.Liked))
        advanceUntilIdle()

        assertEquals("user_liked", viewModel.state.value.likedCurrentCard?.userId)
        coVerify(exactly = 1) { getIncomingLikesUseCase() }

        // Select again - should not trigger load
        viewModel.onEvent(TapeEvent.OnTabSelected(TapeTab.Liked))
        advanceUntilIdle()
        coVerify(exactly = 1) { getIncomingLikesUseCase() }
    }

    @Test
    fun `handleLikedSwipe LEFT sends DISLIKE action and clears card`() = runTest {
        val like = createIncomingLike("l1")
        coEvery { getIncomingLikesUseCase() } returns Result.success(listOf(like))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())
        coEvery { sendUserActionUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(TapeEvent.OnTabSelected(TapeTab.Liked))
        advanceUntilIdle()

        viewModel.onEvent(TapeEvent.OnLikedCardSwiped(SwipeDirection.LEFT))
        advanceUntilIdle()

        coVerify { sendUserActionUseCase("l1", UserAction.DISLIKE) }
        assertNull(viewModel.state.value.likedCurrentCard)
        assertTrue(viewModel.state.value.likedIsEmpty)
    }

    // --- Effects Tests ---

    @Test
    fun `OnWhyClicked emits ShowReasonSheet effect`() = runTest {
        val rec = createRecommendation("1", reasons = listOf("Because"))
        coEvery { getRecommendationsUseCase() } returns Result.success(listOf(rec))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())

        viewModel = TapeViewModel(getRecommendationsUseCase, getIncomingLikesUseCase, sendUserActionUseCase, imageUploadUrlRepository)
        advanceUntilIdle()

        val effects = mutableListOf<TapeEffect>()
        val job = launch {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onEvent(TapeEvent.OnWhyClicked)
        advanceUntilIdle()

        assertTrue(effects.any { it is TapeEffect.ShowReasonSheet && it.reasons == listOf("Because") })
        job.cancel()
    }

    // --- Corner Cases ---

    @Test
    fun `OnLikedRetry reloads liked cards`() = runTest {
        coEvery { getIncomingLikesUseCase() } returns Result.success(emptyList())
        
        viewModel.onEvent(TapeEvent.OnLikedRetry)
        advanceUntilIdle()

        coVerify { getIncomingLikesUseCase() }
    }

    @Test
    fun `swipe when no card does nothing`() = runTest {
        // State has no currentCard
        viewModel.onEvent(TapeEvent.OnCardSwiped(SwipeDirection.RIGHT))
        advanceUntilIdle()

        coVerify(exactly = 0) { sendUserActionUseCase(any(), any()) }
    }

    private fun createRecommendation(id: String, reasons: List<String> = emptyList()) = Recommendations(
        userId = id,
        firstName = "Name$id",
        secondName = "Surname",
        age = 20,
        city = "City",
        photoFileKeys = emptyList(),
        description = "Bio",
        explanation = reasons
    )

    private fun createIncomingLike(id: String) = IncomingLike(
        likedByUserId = id,
        firstName = "Liker$id",
        secondName = "Surname",
        age = 20,
        city = "City",
        description = "Bio",
        photoFileKeys = emptyList(),
        createdAt = ""
    )
}
