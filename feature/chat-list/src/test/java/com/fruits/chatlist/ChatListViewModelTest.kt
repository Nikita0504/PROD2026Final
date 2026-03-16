package com.fruits.chatlist

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.repository.ChatRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    @MockK
    private lateinit var chatRepository: ChatRepository

    private lateinit var viewModel: ChatListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        val emptyChatList = emptyList<Chat>()
        every { chatRepository.observeChats() } returns flowOf(emptyChatList)

//        viewModel = ChatListViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sample_test() = runTest {

    }

//    @Test
//    fun `initial state has empty chats list`() = runTest {
//        // Given
//        every { chatRepository.observeChats() } returns flowOf(emptyList())
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertTrue(state.chats.isEmpty())
//        assertFalse(state.isLoading)
//        assertNull(state.error)
//    }

//    @Test
//    fun `initial state loads chats from repository flow`() = runTest {
//        // Given
//        val chatList = listOf(
//            Chat(
//                id = "1",
//                name = "Chat 1",
//                lastMessage = "Last message 1",
//                timestamp = System.currentTimeMillis(),
//                unreadCount = 0,
//                avatarUrl = null
//            ),
//            Chat(
//                id = "2",
//                name = "Chat 2",
//                lastMessage = "Last message 2",
//                timestamp = System.currentTimeMillis(),
//                unreadCount = 2,
//                avatarUrl = "avatar_url"
//            )
//        )
//        every { chatRepository.observeChats() } returns flowOf(chatList)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(2, state.chats.size)
//        assertEquals("Chat 1", state.chats[0].name)
//        assertEquals("Chat 2", state.chats[1].name)
//    }
//
//    @Test
//    fun `onEvent ChatClicked sends navigate effect`() = runTest {
//        // When
//        viewModel.onEvent(ChatListEvent.ChatClicked("chat_123"))
//        advanceUntilIdle()
//
//        // Then - effect would be emitted (tested via effect collection in real scenario)
//        // This test verifies the method doesn't crash
//    }
//
//    @Test
//    fun `onEvent Refresh sets refreshing state`() = runTest {
//        // Given
//        coEvery { chatRepository.refreshChats() } returns Unit
//        every { chatRepository.observeChats() } returns flowOf(emptyList())
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatListEvent.Refresh)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertFalse(state.isRefreshing) // Should complete after refresh
//    }
//
//    @Test
//    fun `onEvent Refresh calls refreshChats on repository`() = runTest {
//        // Given
//        coEvery { chatRepository.refreshChats() } returns Unit
//        every { chatRepository.observeChats() } returns flowOf(emptyList())
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatListEvent.Refresh)
//        advanceUntilIdle()
//
//        // Then
//        coEvery { chatRepository.refreshChats() } wasCalled 1
//    }
//
//    @Test
//    fun `onEvent Refresh when repository throws error sets error state`() = runTest {
//        // Given
//        val error = Exception("Refresh failed")
//        coEvery { chatRepository.refreshChats() } throws error
//        every { chatRepository.observeChats() } returns flowOf(emptyList())
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatListEvent.Refresh)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertFalse(state.isRefreshing)
//    }
//
//    @Test
//    fun `state updates when repository flow emits new data`() = runTest {
//        // Given
//        val initialChats = emptyList<Chat>()
//        val updatedChats = listOf(
//            Chat(
//                id = "1",
//                name = "New Chat",
//                lastMessage = "New message",
//                timestamp = System.currentTimeMillis(),
//                unreadCount = 1,
//                avatarUrl = null
//            )
//        )
//        val stateFlow = MutableStateFlow(initialChats)
//        every { chatRepository.observeChats() } returns stateFlow
//        advanceUntilIdle()
//
//        // When
//        stateFlow.value = updatedChats
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(1, state.chats.size)
//        assertEquals("New Chat", state.chats[0].name)
//    }
//
//    @Test
//    fun `isLoading is false when repository flow emits data`() = runTest {
//        // Given
//        val chatList = listOf(
//            Chat(
//                id = "1",
//                name = "Chat",
//                lastMessage = "Message",
//                timestamp = System.currentTimeMillis(),
//                unreadCount = 0,
//                avatarUrl = null
//            )
//        )
//        every { chatRepository.observeChats() } returns flowOf(chatList)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertFalse(state.isLoading)
//    }
//
//    @Test
//    fun `multiple chats with different unread counts are handled`() = runTest {
//        // Given
//        val chatList = listOf(
//            Chat(id = "1", name = "Chat 1", lastMessage = "Msg", timestamp = 0L, unreadCount = 0, avatarUrl = null),
//            Chat(id = "2", name = "Chat 2", lastMessage = "Msg", timestamp = 0L, unreadCount = 5, avatarUrl = null),
//            Chat(id = "3", name = "Chat 3", lastMessage = "Msg", timestamp = 0L, unreadCount = 10, avatarUrl = null)
//        )
//        every { chatRepository.observeChats() } returns flowOf(chatList)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(3, state.chats.size)
//        assertEquals(0, state.chats[0].unreadCount)
//        assertEquals(5, state.chats[1].unreadCount)
//        assertEquals(10, state.chats[2].unreadCount)
//    }
}
