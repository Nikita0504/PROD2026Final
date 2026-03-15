package com.fruits.network.chat.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCounterpartSchema(
    @SerialName("user_id") val userId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("second_name") val secondName: String,
    @SerialName("photo_file_keys") val photoFileKeys: List<String> = emptyList(),
)

@Serializable
data class ChatListItemSchema(
    @SerialName("chat_id") val chatId: String,
    val status: String,
    val counterpart: ChatCounterpartSchema,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class ChatMessageSchema(
    @SerialName("message_id") val messageId: String,
    @SerialName("sender_user_id") val senderUserId: String,
    @SerialName("created_at") val createdAt: String,
    val text: String,
)

@Serializable
data class ChatDetailSchema(
    @SerialName("chat_id") val chatId: String,
    val status: String,
    val counterpart: ChatCounterpartSchema,
    val messages: List<ChatMessageSchema> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SendMessageRequestSchema(
    val text: String,
)

@Serializable
data class SendMessageResponseSchema(
    @SerialName("message_id") val messageId: String,
    @SerialName("created_at") val createdAt: String,
)

