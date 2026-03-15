package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.domain.repository.TokenRepository

class SendUserActionUseCase(
    private val interactionsRepository: InteractionsRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend operator fun invoke(
        targetUserId: String,
        action: UserAction,
    ): Result<Unit> {
        val accessToken = tokenRepository.getAccessToken()
        return interactionsRepository.sendAction(
            accessToken = accessToken,
            targetUserId = targetUserId,
            action = action,
        )
    }
}

