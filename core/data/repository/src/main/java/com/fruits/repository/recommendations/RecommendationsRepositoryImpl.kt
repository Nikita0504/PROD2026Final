package com.fruits.repository.recommendations

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.repository.RecommendationsRepository
import com.fruits.network.recommendations.service.RecommendationsService
import com.fruits.repository.recommendations.mapper.RecommendationsMapper.toDomain
import com.fruits.repository.util.mapResult

class RecommendationsRepositoryImpl(
    private val service: RecommendationsService
): RecommendationsRepository {
    override suspend fun getRecommendations(accessToken: String): Result<List<Recommendations>> = service.getRecommendations(accessToken).mapResult{it.toDomain()}
}