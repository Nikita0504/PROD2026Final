package com.fruits.domain.usecase.chat

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatsUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(): Flow<List<Chat>> = chatRepository.observeChats()
}

