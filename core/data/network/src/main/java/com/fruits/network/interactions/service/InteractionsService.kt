package com.fruits.network.interactions.service

import com.fruits.logger.Log
import com.fruits.network.Const
import com.fruits.network.interactions.schema.AuditEventReadSchema
import com.fruits.network.interactions.schema.IncomingLikeSchema
import com.fruits.network.interactions.schema.TargetUserIdSchema
import com.fruits.network.interactions.schema.UserActionCreateSchema
import com.fruits.network.interactions.schema.UserReportCreateSchema
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InteractionsService(
    private val client: HttpClient,
) {

    private val baseUrl = "${Const.serverUrl}/api/v1"

    suspend fun sendAction(
        accessToken: String,
        request: UserActionCreateSchema,
    ): ApiResult<Unit> = safeCall {
        Log.d(TAG, "Sending user action: target=${request.targetUserId}, action=${request.action}")

        val url = when (request.action) {
            "LIKE" -> "$baseUrl/interactions/likes"
            "DISLIKE" -> "$baseUrl/interactions/dislikes"
            "BLOCK" -> "$baseUrl/interactions/blocks"
            "UNBLOCK" -> "$baseUrl/interactions/unblocks"
            else -> return@safeCall ApiResult.Error(code = 422, message = "Unknown action: ${request.action}")
        }

        val body = TargetUserIdSchema(targetUserId = request.targetUserId)
        val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
        val result: ApiResult<Unit> = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(TargetUserIdSchema.serializer(), body))
        }.toApiResult(
            401 to "Пользователь не авторизован",
            422 to "Некорректные данные действия",
            503 to "Сервис временно недоступен, попробуйте позже",
        )

        // логируем так же, как сейчас
        when (result) {
            is ApiResult.Success -> Log.i(TAG, "User action sent successfully: target=${request.targetUserId}, action=${request.action}")
            is ApiResult.Error -> Log.w(TAG, "Failed to send user action: ...")
        }

        result
    }

    suspend fun reportUser(
        accessToken: String,
        request: UserReportCreateSchema,
    ): ApiResult<Unit> =
        safeCall {
            Log.d(TAG, "Reporting user: target=${request.targetUserId}, reason=${request.reason}, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<Unit> = client.post("$baseUrl/interactions/reports") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.toApiResult<Unit>(
                401 to "Пользователь не авторизован",
                422 to "Некорректные данные жалобы",
                503 to "Сервис временно недоступен, попробуйте позже",
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "User report sent successfully: target=${request.targetUserId}, reason=${request.reason}")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to send user report: target=${request.targetUserId}, reason=${request.reason}, code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    suspend fun getIncomingLikes(
        accessToken: String,
    ): ApiResult<List<IncomingLikeSchema>> =
        safeCall {
            Log.d(TAG, "Requesting incoming likes, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<List<IncomingLikeSchema>> =
                client.get("$baseUrl/interactions/incoming_likes") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }.toApiResult(
                    401 to "Пользователь не авторизован",
                    503 to "Сервис временно недоступен, попробуйте позже",
                )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Incoming likes fetched: ${result.data.size} items")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to fetch incoming likes: code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    suspend fun getAudit(
        accessToken: String,
    ): ApiResult<List<AuditEventReadSchema>> =
        safeCall {
            Log.d(TAG, "Requesting audit events, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<List<AuditEventReadSchema>> =
                client.get("$baseUrl/interactions/audit") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }.toApiResult(
                    401 to "Пользователь не авторизован",
                    503 to "Сервис временно недоступен, попробуйте позже",
                )

            when (result) {
                is ApiResult.Success ->
                    Log.i(TAG, "Audit events fetched: ${result.data.size} items")
                is ApiResult.Error ->
                    Log.w(TAG, "Failed to fetch audit events: code=${result.code}, message=${result.message}")
            }

            result
        }

    companion object {
        private const val TAG = "InteractionsService"
    }
}

