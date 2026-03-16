package com.fruits.domain.usecase.chat

import com.fruits.domain.repository.ChatRepository
import com.fruits.domain.repository.TokenRepository

class DeleteChatUseCase(
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(chatId: String): Result<Unit> {
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken.isEmpty()) {
            return Result.failure(IllegalStateException("Не авторизован"))
        }
        return chatRepository.deleteChat(accessToken, chatId)
    }
}

