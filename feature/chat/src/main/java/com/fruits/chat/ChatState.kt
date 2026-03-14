package com.fruits.chat

/**
 * Модель сообщения в чате (пока без бэка).
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val timestampMillis: Long = 0L,
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
)

sealed interface ChatEvent {
    data class InputChanged(val value: String) : ChatEvent
    data object SendClicked : ChatEvent
}
