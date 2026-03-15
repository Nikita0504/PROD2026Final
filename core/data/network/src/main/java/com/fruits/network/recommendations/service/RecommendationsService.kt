package com.fruits.network.recommendations.service

import android.util.Log
import com.fruits.network.recommendations.schema.RecommendationsSchema
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class RecommendationsService(
    private val client: HttpClient,
) {

    private val baseUrl = "https://cannily-infinite-boxfish.cloudpub.ru/api/v1/recommendations"

    suspend fun getRecommendations(accessToken: String): ApiResult<List<RecommendationsSchema>> =
        safeCall {
            Log.d(TAG, "Requesting recommendations, token present: ${accessToken.isNotBlank()}")

            val result = client.get("$baseUrl/recommendations") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult<List<RecommendationsSchema>>(
                401 to "Пользователь не авторизован"
            )

            when (result) {
                is ApiResult.Success -> Log.i(
                    TAG,
                    "Recommendations fetched: ${result.data.size} items"
                )

                is ApiResult.Error -> Log.w(
                    TAG,
                    "Failed to fetch recommendations: code=${result.code}, message=${result.message}"
                )
            }

            result
        }


    companion object {
        private const val TAG = "RecommendationsService"
    }
}
