package com.fruits.chatlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.fruits.domain.model.chat.Chat
import org.junit.Rule
import org.junit.Test

class ChatListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgress() {
        val state = ChatListState(isLoading = true, chats = emptyList())

        composeTestRule.setContent {
            ChatListScreen(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithTag("chat_list_progress").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsMessage() {
        val state = ChatListState(isLoading = false, chats = emptyList())

        composeTestRule.setContent {
            ChatListScreen(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithTag("chat_list_empty_message")
            .assertIsDisplayed()
            .assertTextContains("Нет активных чатов")
    }

    @Test
    fun errorState_showsErrorCard() {
        val state = ChatListState(error = "Connection failed", chats = emptyList())

        composeTestRule.setContent {
            ChatListScreen(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithTag("chat_list_error_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_list_error_text").assertTextContains("Connection failed")
    }

    @Test
    fun hasChats_showsChatItemsWithCorrectData() {
        val chat = Chat(
            id = "chat_1",
            name = "Alice",
            lastMessage = "Hey there!",
            timestamp = System.currentTimeMillis(),
            unreadCount = 5,
            avatarUrl = null
        )
        val state = ChatListState(chats = listOf(chat))

        composeTestRule.setContent {
            ChatListScreen(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithTag("chat_item_chat_1").assertIsDisplayed()
        // Use useUnmergedTree = true because semantics are merged into the clickable parent Card
        composeTestRule.onNodeWithTag("chat_name_chat_1", useUnmergedTree = true).assertTextContains("Alice")
        composeTestRule.onNodeWithTag("chat_last_message_chat_1", useUnmergedTree = true).assertTextContains("Hey there!")
        composeTestRule.onNodeWithTag("chat_unread_count_chat_1", useUnmergedTree = true).assertTextContains("5")
        composeTestRule.onNodeWithTag("chat_placeholder_chat_1", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun clickChatItem_emitsEvent() {
        val chat = Chat(id = "1", name = "Test", lastMessage = "", timestamp = 0, unreadCount = 0, avatarUrl = null)
        val state = ChatListState(chats = listOf(chat))
        var clickedChatId: String? = null

        composeTestRule.setContent {
            ChatListScreen(state = state, onEvent = { event ->
                if (event is ChatListEvent.ChatClicked) clickedChatId = event.chatId
            })
        }

        composeTestRule.onNodeWithTag("chat_item_1").performClick()
        assert(clickedChatId == "1")
    }
}
