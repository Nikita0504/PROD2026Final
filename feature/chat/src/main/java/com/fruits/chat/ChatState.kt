package com.fruits.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val timestampMillis: Long = 0L,
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val chatTitle: String = "Чат",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ChatEvent {
    data class InputChanged(val value: String) : ChatEvent
    data object SendClicked : ChatEvent
}
