package com.fruits.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.fruits.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val chatId: String = savedStateHandle.toRoute<Route.Chat>().chatId
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
        val (title, messages) = when (chatId) {
            "1" -> "Поддержка" to listOf(
                ChatMessage("m1", "Здравствуйте! Чем можем помочь?", false, System.currentTimeMillis() - 120_000),
                ChatMessage("m2", "Есть вопрос по заказу #12345", true, System.currentTimeMillis() - 90_000),
                ChatMessage("m3", "Проверяем, ответим в течение часа.", false, System.currentTimeMillis() - 60_000),
                ChatMessage("m4", "Спасибо!", true, System.currentTimeMillis() - 30_000),
            )
            "2" -> "Команда проекта" to listOf(
                ChatMessage("m1", "Ревью готово, можно мержить", false, System.currentTimeMillis() - 300_000),
                ChatMessage("m2", "Ок, мержу в main", true, System.currentTimeMillis() - 240_000),
                ChatMessage("m3", "Деплой на стейдж в 18:00", false, System.currentTimeMillis() - 180_000),
            )
            "3" -> "Друзья" to listOf(
                ChatMessage("m1", "Привет! Как дела?", false, System.currentTimeMillis() - 600_000),
                ChatMessage("m2", "Нормально, работаю над проектом.", true, System.currentTimeMillis() - 540_000),
                ChatMessage("m3", "Круто. Вечером созвонимся?", false, System.currentTimeMillis() - 300_000),
                ChatMessage("m4", "Да, в 20:00 ок?", true, System.currentTimeMillis() - 120_000),
            )
            else -> "Чат $chatId" to emptyList()
        }
        _state.update {
            it.copy(
                chatTitle = title,
                messages = messages,
            )
        }
    }
}
