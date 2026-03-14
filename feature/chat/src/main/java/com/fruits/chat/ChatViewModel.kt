package com.fruits.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        loadMockMessages()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> {
                _state.update { it.copy(inputText = event.value) }
            }
            ChatEvent.SendClicked -> sendMessage()
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isOwn = true,
            timestampMillis = System.currentTimeMillis(),
        )
        _state.update {
            it.copy(
                messages = it.messages + message,
                inputText = "",
            )
        }
    }

    private fun loadMockMessages() {
        _state.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        text = "Привет! Как дела?",
                        isOwn = false,
                        timestampMillis = System.currentTimeMillis() - 60_000,
                    ),
                    ChatMessage(
                        id = "2",
                        text = "Нормально, работаю над проектом.",
                        isOwn = true,
                        timestampMillis = System.currentTimeMillis() - 50_000,
                    ),
                    ChatMessage(
                        id = "3",
                        text = "Круто. Когда покажешь?",
                        isOwn = false,
                        timestampMillis = System.currentTimeMillis() - 40_000,
                    ),
                ),
            )
        }
    }
}
