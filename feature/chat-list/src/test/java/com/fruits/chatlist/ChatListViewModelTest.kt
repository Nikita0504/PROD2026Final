package com.fruits.chatlist

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.usecase.chat.ObserveChatsUseCase
import com.fruits.domain.usecase.chat.RefreshChatsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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

    private lateinit var viewModel: ChatListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { observeChatsUseCase() } returns MutableStateFlow(emptyList())
        coEvery { refreshChatsUseCase() } returns Unit

        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state observes chats and triggers load`() = runTest {
        val chats = listOf(mockk<Chat>())
        val chatsFlow = MutableStateFlow(emptyList<Chat>())
        every { observeChatsUseCase() } returns chatsFlow
        
        // Re-init to use the new flow
        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase)
        
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
        viewModel = ChatListViewModel(observeChatsUseCase, refreshChatsUseCase)
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
}
