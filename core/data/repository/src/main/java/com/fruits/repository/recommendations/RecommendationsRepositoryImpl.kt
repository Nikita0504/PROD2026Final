package com.fruits.repository.recommendations

import com.fruits.debug.Log
import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.repository.RecommendationsRepository
import com.fruits.network.recommendations.service.RecommendationsService
import com.fruits.network.util.ApiResult
import com.fruits.repository.recommendations.mapper.RecommendationsMapper.toDomain
import com.fruits.repository.util.mapResult
import com.fruits.repository.util.mockOr

class RecommendationsRepositoryImpl(
    private val service: RecommendationsService,
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService
) : RecommendationsRepository {
    override suspend fun getRecommendations(accessToken: String): Result<List<Recommendations>> {
        return mockOr(mockStorage, { mockDataService.recommendations }) {
            val apiResult = service.getRecommendations(accessToken)
            if (apiResult is ApiResult.Error) {
                return@mockOr Result.failure(Exception("[${apiResult.code}] ${apiResult.message}"))
            }
            val schemas = (apiResult as ApiResult.Success).data.candidates
            Log.d(TAG, "=== Got ${schemas.size} candidates ===")
            schemas.forEachIndexed { i, s ->
                Log.d(TAG, "[$i] userId=${s.userId}  name=${s.firstName} ${s.secondName}  age=${s.age}  city=${s.city}")
                Log.d(TAG, "[$i] photoFileKeys(${s.photoFileKeys.size})=${s.photoFileKeys}")
                Log.d(TAG, "[$i] explanation(${s.explanation.size})=${s.explanation}")
            }

            ApiResult.Success(schemas).mapResult { list ->
                list.map { schema ->
                    schema.toDomain()
                }
            }
        }
    }

    companion object {
        private const val TAG = "RecommendationsRepo"
    }
}
