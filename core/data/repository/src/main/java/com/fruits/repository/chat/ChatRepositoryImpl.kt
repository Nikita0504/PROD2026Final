package com.fruits.repository.chat

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.model.chat.ChatDetail
import com.fruits.domain.model.chat.ChatMessage
import com.fruits.domain.model.chat.SentMessage
import com.fruits.domain.repository.ChatRepository
import com.fruits.network.chat.schema.ChatDetailSchema
import com.fruits.network.chat.schema.ChatListItemSchema
import com.fruits.network.chat.schema.ChatMessageSchema
import com.fruits.network.chat.schema.SendMessageResponseSchema
import com.fruits.network.chat.service.ChatService
import com.fruits.repository.util.mapResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepositoryImpl(
    private val service: ChatService,
) : ChatRepository {

    private val chatsFlow = MutableStateFlow<List<Chat>>(emptyList())

    override fun observeChats(): Flow<List<Chat>> = chatsFlow.asStateFlow()

    override suspend fun refreshChats(accessToken: String) {
        val result = service.getChats(accessToken).mapResult { list ->
            list.map { it.toDomain() }
        }
        result.onSuccess { chatsFlow.value = it }
        result.getOrThrow()
    }

    override suspend fun getChat(
        accessToken: String,
        chatId: String,
    ): Result<ChatDetail> {
        return service
            .getChat(accessToken, chatId)
            .mapResult { it.toDomain() }
    }

    override suspend fun deleteChat(
        accessToken: String,
        chatId: String,
    ): Result<Unit> {
        return service
            .deleteChat(accessToken, chatId)
            .mapResult { Unit }
    }

    override suspend fun sendMessage(
        accessToken: String,
        chatId: String,
        text: String,
    ): Result<SentMessage> {
        return service
            .sendMessage(accessToken, chatId, text)
            .mapResult { it.toDomain() }
    }

    private fun ChatListItemSchema.toDomain(): Chat {
        val title = "${counterpart.firstName} ${counterpart.secondName}".trim()
        return Chat(
            id = chatId,
            name = if (title.isBlank()) chatId else title,
            lastMessage = "",
            timestamp = 0L,
            unreadCount = 0,
            avatarUrl = counterpart.photoFileKeys.firstOrNull(),
        )
    }

    private fun ChatDetailSchema.toDomain(): ChatDetail {
        val title = "${counterpart.firstName} ${counterpart.secondName}".trim()
        return ChatDetail(
            id = chatId,
            title = if (title.isBlank()) chatId else title,
            status = status,
            messages = messages.map { it.toDomain() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun ChatMessageSchema.toDomain(): ChatMessage {
        return ChatMessage(
            id = messageId,
            senderUserId = senderUserId,
            text = text,
            createdAt = createdAt,
        )
    }

    private fun SendMessageResponseSchema.toDomain(): SentMessage {
        return SentMessage(
            id = messageId,
            createdAt = createdAt,
        )
    }
}

