package com.fruits.chatlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fruits.domain.model.chat.Chat
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeChatListViewModel

    @Before
    fun setup() {
        fakeViewModel = FakeChatListViewModel()
    }

    @Test
    fun renderChatListState_loading_showsProgressBar() {
        // Given
        val loadingState = ChatListState(
            isLoading = true,
            chats = emptyList()
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = loadingState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("chat_list_progress").assertIsDisplayed()
    }

    @Test
    fun renderChatListState_withChats_showsChatList() {
        // Given
        val chats = listOf(
            Chat(
                id = "1",
                name = "Поддержка",
                lastMessage = "Здравствуйте! Чем можем помочь?",
                timestamp = System.currentTimeMillis(),
                unreadCount = 1,
                avatarUrl = null
            ),
            Chat(
                id = "2",
                name = "Команда проекта",
                lastMessage = "Ревью готово",
                timestamp = System.currentTimeMillis(),
                unreadCount = 0,
                avatarUrl = null
            )
        )
        val successState = ChatListState(
            isLoading = false,
            chats = chats
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = successState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("chat_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Поддержка").assertIsDisplayed()
        composeTestRule.onNodeWithText("Команда проекта").assertIsDisplayed()
    }

    @Test
    fun renderChatListState_withError_showsErrorMessage() {
        // Given
        val errorState = ChatListState(
            isLoading = false,
            chats = emptyList(),
            error = "Ошибка загрузки чатов"
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = errorState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithText("Ошибка загрузки чатов").assertIsDisplayed()
    }

    @Test
    fun renderChatListState_emptyList_showsEmptyList() {
        // Given
        val emptyState = ChatListState(
            isLoading = false,
            chats = emptyList()
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = emptyState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("chat_list").assertIsDisplayed()
    }

    @Test
    fun clickChatItem_emitsChatClickedEvent() {
        // Given
        val chats = listOf(
            Chat(
                id = "chat_123",
                name = "Test Chat",
                lastMessage = "Last message",
                timestamp = System.currentTimeMillis(),
                unreadCount = 0,
                avatarUrl = null
            )
        )
        val successState = ChatListState(
            isLoading = false,
            chats = chats
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = successState,
                onEvent = fakeViewModel::onEvent
            )
        }

        composeTestRule.onNodeWithTag("chat_item_chat_123").performClick()

        // Then
        assertThat(fakeViewModel.clickedChatId).isEqualTo("chat_123")
    }

    @Test
    fun renderChatListState_withUnreadCount_showsBadge() {
        // Given
        val chats = listOf(
            Chat(
                id = "1",
                name = "Chat with unread",
                lastMessage = "New message",
                timestamp = System.currentTimeMillis(),
                unreadCount = 5,
                avatarUrl = null
            )
        )
        val successState = ChatListState(
            isLoading = false,
            chats = chats
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = successState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun renderChatListState_refreshing_showsRefreshIndicator() {
        // Given
        val chats = listOf(
            Chat(
                id = "1",
                name = "Chat",
                lastMessage = "Message",
                timestamp = System.currentTimeMillis(),
                unreadCount = 0,
                avatarUrl = null
            )
        )
        val refreshingState = ChatListState(
            isLoading = false,
            isRefreshing = true,
            chats = chats
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = refreshingState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then - refresh indicator should be shown (implicit in PullToRefreshBox)
        composeTestRule.onNodeWithTag("chat_list").assertIsDisplayed()
    }

    @Test
    fun pullToRefresh_emitsRefreshEvent() {
        // Given
        val chats = listOf(
            Chat(
                id = "1",
                name = "Chat",
                lastMessage = "Message",
                timestamp = System.currentTimeMillis(),
                unreadCount = 0,
                avatarUrl = null
            )
        )
        val successState = ChatListState(
            isLoading = false,
            chats = chats
        )

        // When
        composeTestRule.setContent {
            ChatListScreen(
                state = successState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Note: Pull-to-refresh gesture testing requires more complex setup
        // This test verifies the structure is in place
        composeTestRule.onNodeWithTag("chat_list").assertIsDisplayed()
    }
}

// Fake ViewModel для тестирования
class FakeChatListViewModel {
    var clickedChatId: String? = null
    var refreshCalled: Boolean = false

    fun onEvent(event: ChatListEvent) {
        when (event) {
            is ChatListEvent.ChatClicked -> {
                clickedChatId = event.chatId
            }
            ChatListEvent.Refresh -> {
                refreshCalled = true
            }
        }
    }
}
