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

    @Test
    fun `initialization loads recommendations`() = runTest {
        val recommendation = Recommendations(
            userId = "1",
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

        // Re-init to trigger load
        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.currentCard)
        assertEquals("1", state.currentCard?.userId)
        assertEquals("http://image.com/1", state.currentCard?.imageUrl)
    }

    @Test
    fun `onTabSelected Liked loads liked cards if empty`() = runTest {
        val incomingLike = IncomingLike(
            likedByUserId = "user2",
            firstName = "Jane",
            secondName = "Doe",
            age = 22,
            city = "SPB",
            description = "Hi",
            photoFileKeys = listOf("key2"),
            createdAt = "2023-01-01"
        )
        val downloadUrl = DownloadUrlData(key = "key2", url = "http://image.com/2")

        coEvery { getIncomingLikesUseCase() } returns Result.success(listOf(incomingLike))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(listOf(downloadUrl))

        viewModel.onEvent(TapeEvent.OnTabSelected(TapeTab.Liked))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TapeTab.Liked, state.selectedTab)
        assertEquals(1, state.likedCards.size)
        assertEquals("user2", state.likedCards[0].userId)
    }

    @Test
    fun `handleSwipe RIGHT sends LIKE action`() = runTest {
        val recommendation = Recommendations(
            userId = "1",
            firstName = "John",
            secondName = "Doe",
            age = 25,
            city = "Moscow",
            photoFileKeys = listOf("key1"),
            description = "About me",
            explanation = listOf("Reason 1")
        )
        coEvery { getRecommendationsUseCase() } returns Result.success(listOf(recommendation))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())
        coEvery { sendUserActionUseCase(any(), any()) } returns Result.success(Unit)

        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
        advanceUntilIdle()

        viewModel.onEvent(TapeEvent.OnCardSwiped(SwipeDirection.RIGHT))
        advanceUntilIdle()

        coVerify { sendUserActionUseCase("1", UserAction.LIKE) }
    }

    @Test
    fun `handleSwipe LEFT sends DISLIKE action`() = runTest {
        val recommendation = Recommendations(
            userId = "1",
            firstName = "John",
            secondName = "Doe",
            age = 25,
            city = "Moscow",
            photoFileKeys = listOf("key1"),
            description = "About me",
            explanation = listOf("Reason 1")
        )
        coEvery { getRecommendationsUseCase() } returns Result.success(listOf(recommendation))
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())
        coEvery { sendUserActionUseCase(any(), any()) } returns Result.success(Unit)

        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
        advanceUntilIdle()

        viewModel.onEvent(TapeEvent.OnCardSwiped(SwipeDirection.LEFT))
        advanceUntilIdle()

        coVerify { sendUserActionUseCase("1", UserAction.DISLIKE) }
    }

    @Test
    fun `loadRecommendations failure sets error state`() = runTest {
        coEvery { getRecommendationsUseCase() } returns Result.failure(Exception("Network error"))

        viewModel = TapeViewModel(
            getRecommendationsUseCase,
            getIncomingLikesUseCase,
            sendUserActionUseCase,
            imageUploadUrlRepository
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Network error", state.error)
        assertFalse(state.isLoading)
    }
}
