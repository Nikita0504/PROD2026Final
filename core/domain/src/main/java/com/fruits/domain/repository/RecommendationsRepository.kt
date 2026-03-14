package com.fruits.domain.repository

import com.fruits.domain.model.recommendations.Recommendations

interface RecommendationsRepository {
    suspend fun getRecommendations(accessToken: String): Result<List<Recommendations>>
}