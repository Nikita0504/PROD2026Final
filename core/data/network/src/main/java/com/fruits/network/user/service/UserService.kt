package com.fruits.network.user.service

import com.fruits.logger.Log
import com.fruits.network.Const.serverUrl
import com.fruits.network.user.schema.RefreshTokenSchema
import com.fruits.network.user.schema.TokenReadSchema
import com.fruits.network.user.schema.UserCreateSchema
import com.fruits.network.user.schema.UserLoginSchema
import com.fruits.network.user.schema.UserProfileUpdateScheme
import com.fruits.network.user.schema.UserReadSchema
import com.fruits.network.user.schema.UserRegisterSchema
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserService(
    private val client: HttpClient,
) {
    private val baseUrl = "$serverUrl/api/v1/users"

    suspend fun login(body: UserLoginSchema): ApiResult<TokenReadSchema> = safeCall {
        Log.d(TAG, "Attempting login for email: ${body.email}")

        val result = client.post("$baseUrl/auth/login") {
            jsonBody(body)
        }.toApiResult<TokenReadSchema>(
            403 to "Неверный пароль",
            404 to "Пользователь с таким email не найден",
            422 to "Ошибка валидации данных"
        )

        when (result) {
            is ApiResult.Success -> Log.i(TAG, "Login successful for email: ${body.email}")
            is ApiResult.Error   -> Log.w(TAG, "Login failed for email: ${body.email}, code=${result.code}, message=${result.message}")
        }

        result
    }

    suspend fun refreshToken(refreshToken: String): ApiResult<TokenReadSchema> = safeCall {
        Log.d(TAG, "Attempting token refresh, token present: ${refreshToken.isNotBlank()}")

        val result = client.post("$baseUrl/token/refresh") {
            jsonBody(RefreshTokenSchema(refreshToken))
        }.toApiResult<TokenReadSchema>(
            401 to "Refresh токен недействителен"
        )

        when (result) {
            is ApiResult.Success -> Log.i(TAG, "Token refreshed successfully")
            is ApiResult.Error   -> Log.w(TAG, "Token refresh failed: code=${result.code}, message=${result.message}")
        }

        result
    }

    suspend fun getProfile(accessToken: String): ApiResult<UserReadSchema> = safeCall {
        Log.d(TAG, "Fetching user profile, token present: ${accessToken.isNotBlank()}")

        val result = client.get("$baseUrl/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.toApiResult<UserReadSchema>(
            401 to "Пользователь не авторизован"
        )

        when (result) {
            is ApiResult.Success -> Log.i(TAG, "Profile fetched: userId=${result.data.id}, email=${result.data.email}")
            is ApiResult.Error   -> Log.w(TAG, "Failed to fetch profile: code=${result.code}, message=${result.message}")
        }

        result
    }

    suspend fun patchProfile(updateData: UserProfileUpdateScheme, accessToken: String): ApiResult<UserReadSchema> = safeCall {
        Log.d(TAG, "Patching user profile with fields: $updateData")

        val result = client.patch("$baseUrl/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            jsonBody(updateData)
        }.toApiResult<UserReadSchema>(
            401 to "Пользователь не авторизован"
        )

        when (result) {
            is ApiResult.Success -> Log.i(TAG, "Profile updated successfully: userId=${result.data.id}")
            is ApiResult.Error   -> Log.w(TAG, "Profile update failed: code=${result.code}, message=${result.message}")
        }

        result
    }

    suspend fun updateFcmToken(token: String, accessToken: String): ApiResult<Unit> = safeCall {
        Log.d(TAG, "Updating FCM token for user $token")

        val result = client.post("$baseUrl/fcm-token") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            jsonBody(com.fruits.network.user.schema.FcmTokenRequest(token))
        }.toApiResult<Unit>()

        when (result) {
            is ApiResult.Success -> Log.i(TAG, "FCM token updated successfully")
            is ApiResult.Error   -> Log.w(TAG, "FCM token update failed: code=${result.code}, message=${result.message}")
        }

        result
    }

    private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    companion object {
        private const val TAG = "UserService"
    }
}
