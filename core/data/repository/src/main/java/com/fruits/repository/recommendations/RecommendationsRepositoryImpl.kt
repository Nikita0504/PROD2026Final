package com.fruits.repository.recommendations

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.repository.RecommendationsRepository
import com.fruits.network.recommendations.service.RecommendationsService
import com.fruits.repository.recommendations.mapper.RecommendationsMapper.toDomain
import com.fruits.repository.util.mapResult
import com.fruits.repository.util.mockOr

class RecommendationsRepositoryImpl(
    private val service: RecommendationsService,
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService
) : RecommendationsRepository {
    override suspend fun getRecommendations(accessToken: String): Result<List<Recommendations>> =
        mockOr(mockStorage, { mockDataService.recommendations }) {
            service.getRecommendations(accessToken).mapResult { it.toDomain() }
        }
}