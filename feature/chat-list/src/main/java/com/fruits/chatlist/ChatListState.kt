package com.fruits.chatlist

import com.fruits.domain.model.chat.Chat

data class ChatListState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

sealed interface ChatListEvent {
    data object Refresh : ChatListEvent
    data class ChatClicked(val chatId: String) : ChatListEvent
}

sealed interface ChatListEffect {
    data class NavigateToChatDetail(val chatId: String) : ChatListEffect
    data object ShowErrorSnackbar : ChatListEffect
}