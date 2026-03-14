package com.fruits.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.repository.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

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
                // Trigger network refresh if needed
                if (isRefresh) {
                    chatRepository.refreshChats()
                }

                // Observe data flow (In real app, you might collect this continuously in init)
                // For simplicity in MVI action-triggered load:
                // Assuming repository exposes a flow we can collect once or we rely on initial collection
                // Better approach: Collect flow in init and update state, use refresh for force update

                // Re-implementing proper flow collection pattern for MVI:
                // The actual collection should happen in init, here we just trigger refresh
                // But to satisfy the "Load" action logic:
                if (!isRefresh) {
                    // Initial load logic handled by init collection below
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null
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

    // Proper Flow Collection for Real-time updates
    init {
        viewModelScope.launch {
            chatRepository.observeChats().collect { chatList ->
                _state.update {
                    it.copy(
                        chats = chatList,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun navigateToChat(chatId: String) {
        viewModelScope.launch {
            _effect.send(ChatListEffect.NavigateToChatDetail(chatId))
        }
    }

    private suspend fun sendEffect(effect: ChatListEffect) {
        _effect.send(effect)
    }
}