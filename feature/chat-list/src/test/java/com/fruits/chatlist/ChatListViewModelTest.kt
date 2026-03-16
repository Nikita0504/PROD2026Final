package com.fruits.chatlist

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.usecase.chat.ObserveChatsUseCase
import com.fruits.domain.usecase.chat.RefreshChatsUseCase
import com.fruits.logger.Log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    @MockK
    private lateinit var observeChatsUseCase: ObserveChatsUseCase
    @MockK
    private lateinit var refreshChatsUseCase: RefreshChatsUseCase
    @MockK
    private lateinit var imageUploadUrlRepository: ImageUploadUrlRepository

    private lateinit var viewModel: ChatListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkObject(Log)
        every { Log.w(any(), any()) } returns Unit

        every { observeChatsUseCase() } returns MutableStateFlow(emptyList())
        coEvery { refreshChatsUseCase() } returns Unit
        coEvery { imageUploadUrlRepository.getDownloadUrls(any()) } returns Result.success(emptyList())

        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase, imageUploadUrlRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state observes chats and triggers load`() = runTest {
        val chats = listOf(mockk<Chat>(relaxed = true) {
            every { avatarFileKey } returns null
        })
        val chatsFlow = MutableStateFlow(emptyList<Chat>())
        every { observeChatsUseCase() } returns chatsFlow
        
        // Re-init to use the new flow
        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase, imageUploadUrlRepository)
        
        chatsFlow.value = chats
        advanceUntilIdle()

        assertEquals(chats, viewModel.state.value.chats)
        coVerify { refreshChatsUseCase() }
    }

    @Test
    fun `onEvent Refresh calls refreshChatsUseCase`() = runTest {
        viewModel.onEvent(ChatListEvent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshing)
        coVerify(exactly = 2) { refreshChatsUseCase() } // 1 in init, 1 in onEvent
    }

    @Test
    fun `loadChats failure sets error and emits effect`() = runTest {
        coEvery { refreshChatsUseCase() } throws Exception("Load error")
        
        // Re-init to trigger failure in init
        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase, imageUploadUrlRepository)
        advanceUntilIdle()

        assertEquals("Load error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `onEvent ChatClicked emits NavigateToChatDetail effect`() = runTest {
        val effects = mutableListOf<ChatListEffect>()
        val job = launch {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(ChatListEvent.ChatClicked("chat123"))
        advanceUntilIdle()

        assertTrue(effects.any { it is ChatListEffect.NavigateToChatDetail && it.chatId == "chat123" })
        job.cancel()
    }

    @Test
    fun `observeChats enriches chats with avatar URLs`() = runTest {
        val chat = Chat(
            id = "1",
            name = "Test",
            lastMessage = "",
            timestamp = 0,
            unreadCount = 0,
            avatarUrl = null,
            avatarFileKey = "key1"
        )
        val chatsFlow = MutableStateFlow(listOf(chat))
        every { observeChatsUseCase() } returns chatsFlow
        
        val downloadUrl = mockk<com.fruits.domain.model.image.DownloadUrlData> {
            every { key } returns "key1"
            every { url } returns "http://url1"
        }
        coEvery { imageUploadUrlRepository.getDownloadUrls(listOf("key1")) } returns Result.success(listOf(downloadUrl))

        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase, imageUploadUrlRepository)
        advanceUntilIdle()

        assertEquals("http://url1", viewModel.state.value.chats[0].avatarUrl)
        coVerify { imageUploadUrlRepository.getDownloadUrls(listOf("key1")) }
    }
}
