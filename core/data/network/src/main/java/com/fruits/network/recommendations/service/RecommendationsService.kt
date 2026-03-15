package com.fruits.network.recommendations.service

import android.util.Log
import com.fruits.network.recommendations.schema.RecommendationsResponse
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

    private val baseUrl = "https://team-25-backend-machine-bdf8bd.pages.prodcontest.ru/api/v1"

    suspend fun getRecommendations(accessToken: String): ApiResult<RecommendationsResponse> =
        safeCall {
            Log.d(TAG, "Requesting recommendations, token present: ${accessToken.isNotBlank()}")

            val result = client.get("$baseUrl/recommendations") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult<RecommendationsResponse>(
                401 to "Пользователь не авторизован",
                503 to "Сервис временно недоступен, попробуйте позже"
            )

            when (result) {
                is ApiResult.Success -> Log.i(
                    TAG,
                    "Recommendations fetched: ${result.data.candidates.size} candidates"
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
