package com.fruits.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgress() {
        val state = ChatState(isLoading = true, messages = emptyList())

        composeTestRule.setContent {
            ChatScreenWrapper(state = state)
        }

        composeTestRule.onNodeWithTag("chat_progress").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        val state = ChatState(error = "Connection timeout", messages = emptyList())

        composeTestRule.setContent {
            ChatScreenWrapper(state = state)
        }

        composeTestRule.onNodeWithTag("chat_error_text").assertTextContains("Connection timeout")
    }

    @Test
    fun hasMessages_showsMessagesList() {
        val messages = listOf(
            ChatMessage(id = "m1", text = "Hello!", isOwn = false),
            ChatMessage(id = "m2", text = "Hi there", isOwn = true)
        )
        val state = ChatState(messages = messages, chatTitle = "Ivan")

        composeTestRule.setContent {
            ChatScreenWrapper(state = state)
        }

        composeTestRule.onNodeWithTag("chat_title").assertTextContains("Ivan")
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_bubble_m1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_bubble_m2").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun inputMessage_updatesInputField() {
        var inputVal = ""
        val state = ChatState(inputText = "typing...")

        composeTestRule.setContent {
            ChatScreenWrapper(
                state = state,
                onEvent = { if (it is ChatEvent.InputChanged) inputVal = it.value }
            )
        }

        composeTestRule.onNodeWithTag("chat_input_field").assertTextContains("typing...")
        composeTestRule.onNodeWithTag("chat_input_field")
            .performClick()
            .performKeyInput { keyDown(Key.MoveEnd) }
            .performTextInput(" more")
        assert(inputVal == "typing... more", lazyMessage = {inputVal})
    }

    @Test
    fun clickSend_emitsEvent() {
        var sendClicked = false
        val state = ChatState(inputText = "Hello")

        composeTestRule.setContent {
            ChatScreenWrapper(
                state = state,
                onEvent = { if (it is ChatEvent.SendClicked) sendClicked = true }
            )
        }

        composeTestRule.onNodeWithTag("chat_send_button").performClick()
        assert(sendClicked)
    }

    @Composable
    private fun ChatScreenWrapper(
        state: ChatState,
        onEvent: (ChatEvent) -> Unit = {}
    ) {
        ChatScreen(
            state = state,
            onEvent = onEvent,
            onBack = {}
        )
    }
}
