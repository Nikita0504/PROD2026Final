package com.fruits.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fruits.domain.usecase.chat.GetChatUseCase
import com.fruits.domain.usecase.chat.SendChatMessageUseCase
import com.fruits.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChatUseCase: GetChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) : ViewModel() {

    val chatId: String = savedStateHandle.toRoute<Route.Chat>().chatId
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        loadChat()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> {
                _state.update { it.copy(inputText = event.value) }
            }

            ChatEvent.SendClicked -> sendMessage()
        }
    }

    private fun loadChat() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = getChatUseCase(chatId)

            result
                .onSuccess { detail ->
                    val messages = detail.messages.map { domainMessage ->
                        ChatMessage(
                            id = domainMessage.id,
                            text = domainMessage.text,
                            isOwn = false,
                            timestampMillis = 0L,
                        )
                    }

                    _state.update {
                        it.copy(
                            chatTitle = detail.title,
                            messages = messages,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось загрузить чат",
                        )
                    }
                }
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            // оптимистичное добавление сообщения
            val localMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = text,
                isOwn = true,
                timestampMillis = System.currentTimeMillis(),
            )

            _state.update {
                it.copy(
                    messages = it.messages + localMessage,
                    inputText = "",
                )
            }

            val result = sendChatMessageUseCase(chatId, text)

            result.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message ?: "Не удалось отправить сообщение",
                    )
                }
            }
        }
    }
}
