package com.fruits.network.user.service

import com.fruits.network.user.schema.RefreshTokenSchema
import com.fruits.network.user.schema.TokenReadSchema
import com.fruits.network.user.schema.UserCreateSchema
import com.fruits.network.user.schema.UserLoginSchema
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

    private val baseUrl = "/api/v1/users"

    suspend fun register(body: UserCreateSchema): ApiResult<UserRegisterSchema> = safeCall {
        client.post("$baseUrl/auth/register") {
            jsonBody(body)
        }.toApiResult(
            409 to "Пользователь с таким email уже зарегистрирован"
        )
    }

    suspend fun login(body: UserLoginSchema): ApiResult<TokenReadSchema> = safeCall {
        client.post("$baseUrl/auth/login") {
            jsonBody(body)
        }.toApiResult(
            403 to "Неверный пароль",
            404 to "Пользователь с таким email не найден",
            422 to "Ошибка валидации данных"
        )
    }

    suspend fun refreshToken(refreshToken: String): ApiResult<TokenReadSchema> = safeCall{
        client.post("$baseUrl/users/token/refresh") {
            jsonBody(RefreshTokenSchema(refreshToken))
        }.toApiResult(
            401 to "Refresh токен недействителен",

            )
    }

    suspend fun getProfile(accessToken: String): ApiResult<UserReadSchema> = safeCall {
        client.get("$baseUrl/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.toApiResult(
            401 to "Пользователь не авторизован"
        )
    }

    private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
