package com.fruits.network.interactions.service

import android.util.Log
import com.fruits.network.Const
import com.fruits.network.interactions.schema.UserActionCreateSchema
import com.fruits.network.interactions.schema.UserReportCreateSchema
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders

class InteractionsService(
    private val client: HttpClient,
) {

    private val baseUrl = "${Const.serverUrl}/api/v1"

    suspend fun sendAction(
        accessToken: String,
        request: UserActionCreateSchema,
    ): ApiResult<Unit> =
        safeCall {
            Log.d(TAG, "Sending user action: target=${request.targetUserId}, action=${request.action}")

            client.post("$baseUrl/interactions/actions") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(request)
            }.toApiResult(
                401 to "Пользователь не авторизован",
                422 to "Некорректные данные действия",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    suspend fun reportUser(
        accessToken: String,
        request: UserReportCreateSchema,
    ): ApiResult<Unit> =
        safeCall {
            Log.d(TAG, "Reporting user: target=${request.targetUserId}, reason=${request.reason}")

            client.post("$baseUrl/interactions/reports") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(request)
            }.toApiResult(
                401 to "Пользователь не авторизован",
                422 to "Некорректные данные жалобы",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    companion object {
        private const val TAG = "InteractionsService"
    }
}

