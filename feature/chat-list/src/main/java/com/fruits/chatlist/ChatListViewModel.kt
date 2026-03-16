package com.fruits.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.chat.ObserveChatsUseCase
import com.fruits.domain.usecase.chat.RefreshChatsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val observeChatsUseCase: ObserveChatsUseCase,
    private val refreshChatsUseCase: RefreshChatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    private val _effect = Channel<ChatListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeChats()
        loadChats()
    }

    fun onEvent(event: ChatListEvent) {
        when (event) {
            is ChatListEvent.Refresh -> loadChats(isRefresh = true)
            is ChatListEvent.ChatClicked -> navigateToChat(event.chatId)
        }
    }

    private fun observeChats() {
        viewModelScope.launch {
            observeChatsUseCase().collectLatest { chats ->
                _state.update { it.copy(chats = chats) }
            }
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
                refreshChatsUseCase()
                _state.update {
                    it.copy(
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
