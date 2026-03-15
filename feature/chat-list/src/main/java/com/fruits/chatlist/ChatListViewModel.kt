package com.fruits.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.model.chat.Chat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    private val _effect = Channel<ChatListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadChats()
    }

    fun onEvent(event: ChatListEvent) {
        when (event) {
            is ChatListEvent.Refresh -> loadChats(isRefresh = true)
            is ChatListEvent.ChatClicked -> navigateToChat(event.chatId)
        }
    }

    private fun loadChats(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.update { it.copy(isLoading = true, error = null) }
            } else {
                _state.update { it.copy(isRefreshing = true) }
            }

            try {
                val mockChats = listOf(
                    Chat(
                        id = "1",
                        name = "Поддержка",
                        lastMessage = "Здравствуйте! Чем можем помочь?",
                        timestamp = System.currentTimeMillis() - 3600_000,
                        unreadCount = 2,
                        avatarUrl = null,
                    ),
                    Chat(
                        id = "2",
                        name = "Команда проекта",
                        lastMessage = "Ревью готово, можно мержить",
                        timestamp = System.currentTimeMillis() - 86400_000,
                        unreadCount = 0,
                        avatarUrl = null,
                    ),
                    Chat(
                        id = "3",
                        name = "Друзья",
                        lastMessage = "Вечером созвонимся?",
                        timestamp = System.currentTimeMillis() - 300_000,
                        unreadCount = 1,
                        avatarUrl = null,
                    ),
                )
                _state.update {
                    it.copy(
                        chats = mockChats,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Unknown error"
                    )
                }
                _effect.send(ChatListEffect.ShowErrorSnackbar)
            }
        }
    }

    private fun navigateToChat(chatId: String) {
        viewModelScope.launch {
            _effect.send(ChatListEffect.NavigateToChatDetail(chatId))
        }
    }
}
