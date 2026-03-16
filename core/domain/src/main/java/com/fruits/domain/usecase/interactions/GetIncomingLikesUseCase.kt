package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.domain.repository.TokenRepository

class GetIncomingLikesUseCase(
    private val interactionsRepository: InteractionsRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend operator fun invoke(): Result<List<IncomingLike>> {
        val accessToken = tokenRepository.getAccessToken()
        return interactionsRepository.getIncomingLikes(accessToken)
    }
}
