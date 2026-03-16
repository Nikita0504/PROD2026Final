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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ChatService(
    private val client: HttpClient,
) {

    private val baseUrl = "${Const.serverUrl}/api/v1/chats"

    suspend fun getChats(accessToken: String): ApiResult<List<ChatListItemSchema>> =
        safeCall {
            Log.d(TAG, "Requesting chats list, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<List<ChatListItemSchema>> = client.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult<List<ChatListItemSchema>>(
                401 to "Пользователь не авторизован",
                503 to "Сервис временно недоступен, попробуйте позже",
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Chats fetched: count=${result.data.size}")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to fetch chats: code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    suspend fun getChat(
        accessToken: String,
        chatId: String,
    ): ApiResult<ChatDetailSchema> =
        safeCall {
            Log.d(TAG, "Requesting chat details, chatId=$chatId, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<ChatDetailSchema> = client.get("$baseUrl/$chatId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult<ChatDetailSchema>(
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Chat details fetched: chatId=${result.data.chatId}, messages=${result.data.messages.size}")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to fetch chat details: chatId=$chatId, code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    suspend fun deleteChat(
        accessToken: String,
        chatId: String,
    ): ApiResult<Unit> =
        safeCall {
            Log.d(TAG, "Deleting chat, chatId=$chatId, token present: ${accessToken.isNotBlank()}")

            val result: ApiResult<Unit> = client.delete("$baseUrl/$chatId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }.toApiResult<Unit>(
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Chat deleted successfully: chatId=$chatId")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to delete chat: chatId=$chatId, code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    suspend fun sendMessage(
        accessToken: String,
        chatId: String,
        text: String,
    ): ApiResult<SendMessageResponseSchema> =
        safeCall {
            Log.d(TAG, "Sending message to chat=$chatId, textLength=${text.length}")

            val result: ApiResult<SendMessageResponseSchema> = client.post("$baseUrl/$chatId/messages") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(SendMessageRequestSchema(text = text))
            }.toApiResult<SendMessageResponseSchema>(
                401 to "Пользователь не авторизован",
                422 to "Ошибка валидации данных",
                503 to "Сервис временно недоступен, попробуйте позже",
            )

            when (result) {
                is ApiResult.Success -> {
                    Log.i(TAG, "Message sent: chatId=$chatId, messageId=${result.data.messageId}")
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Failed to send message: chatId=$chatId, code=${result.code}, message=${result.message}")
                }
            }

            result
        }

    companion object {
        private const val TAG = "ChatService"
    }
}

