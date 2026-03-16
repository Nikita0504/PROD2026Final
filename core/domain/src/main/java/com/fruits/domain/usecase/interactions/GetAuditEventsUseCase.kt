package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.AuditEvent
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.domain.repository.TokenRepository

class GetAuditEventsUseCase(
    private val interactionsRepository: InteractionsRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend operator fun invoke(): Result<List<AuditEvent>> {
        val accessToken = tokenRepository.getAccessToken()
        return interactionsRepository.getAudit(accessToken)
    }
}
