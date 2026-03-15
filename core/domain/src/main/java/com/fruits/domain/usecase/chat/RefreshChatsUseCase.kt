package com.fruits.domain.usecase.chat

import com.fruits.domain.repository.ChatRepository
import com.fruits.domain.repository.TokenRepository

class RefreshChatsUseCase(
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke() {
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken.isEmpty()) {
            throw IllegalStateException("Не авторизован")
        }
        chatRepository.refreshChats(accessToken)
    }
}

