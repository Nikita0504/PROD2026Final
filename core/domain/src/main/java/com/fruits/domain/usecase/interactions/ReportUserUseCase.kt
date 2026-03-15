package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.domain.repository.TokenRepository

class ReportUserUseCase(
    private val interactionsRepository: InteractionsRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend operator fun invoke(
        targetUserId: String,
        reason: ReportReason,
        comment: String?,
    ): Result<Unit> {
        val accessToken = tokenRepository.getAccessToken()
        return interactionsRepository.reportUser(
            accessToken = accessToken,
            targetUserId = targetUserId,
            reason = reason,
            comment = comment,
        )
    }
}

