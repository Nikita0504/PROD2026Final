package com.fruits.network.chat.service

import com.fruits.logger.Log
import com.fruits.network.Const
import com.fruits.network.chat.schema.ChatDetailSchema
import com.fruits.network.chat.schema.ChatListItemSchema
import com.fruits.network.chat.schema.SendMessageRequestSchema
import com.fruits.network.chat.schema.SendMessageResponseSchema
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders

class ChatService(
    private val client: HttpClient,
) {

    private val baseUrl = "${Const.serverUrl}/api/v1/chats"

    suspend fun getChats(accessToken: String): ApiResult<List<ChatListItemSchema>> =
        safeCall {
            Log.d(TAG, "Requesting chats list, token present: ${accessToken.isNotBlank()}")

            client.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult(
                401 to "Пользователь не авторизован",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    suspend fun getChat(
        accessToken: String,
        chatId: String,
    ): ApiResult<ChatDetailSchema> =
        safeCall {
            Log.d(TAG, "Requesting chat details, chatId=$chatId")

            client.get("$baseUrl/$chatId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult(
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    suspend fun deleteChat(
        accessToken: String,
        chatId: String,
    ): ApiResult<Unit> =
        safeCall {
            Log.d(TAG, "Deleting chat, chatId=$chatId")

            client.delete("$baseUrl/$chatId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult(
                204 to Unit,
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    suspend fun sendMessage(
        accessToken: String,
        chatId: String,
        text: String,
    ): ApiResult<SendMessageResponseSchema> =
        safeCall {
            Log.d(TAG, "Sending message to chat=$chatId")

            client.post("$baseUrl/$chatId/messages") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(SendMessageRequestSchema(text = text))
            }.toApiResult(
                201 to "Сообщение успешно отправлено",
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )
        }

    companion object {
        private const val TAG = "ChatService"
    }
}

