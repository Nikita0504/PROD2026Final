package com.fruits.domain.usecase.chat

import com.fruits.domain.model.chat.SentMessage
import com.fruits.domain.repository.ChatRepository
import com.fruits.domain.repository.TokenRepository

class SendChatMessageUseCase(
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(chatId: String, text: String): Result<SentMessage> {
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken.isEmpty()) {
            return Result.failure(IllegalStateException("Не авторизован"))
        }
        return chatRepository.sendMessage(accessToken, chatId, text)
    }
}

