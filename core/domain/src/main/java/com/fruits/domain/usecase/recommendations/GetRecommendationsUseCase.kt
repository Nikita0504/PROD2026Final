package com.fruits.domain.usecase.recommendations

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate
import com.fruits.domain.repository.RecommendationsRepository
import com.fruits.domain.repository.TokenRepository

class GetRecommendationsUseCase(
    private val recommendationsRepository: RecommendationsRepository,
    private val tokensRepository: TokenRepository
) {

    suspend operator fun invoke(): Result<List<Recommendations>> {
        val accessToken = tokensRepository.getAccessToken()
        val result = recommendationsRepository.getRecommendations(accessToken)
        return result
    }

}