package com.fruits.domain.repository

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.model.chat.ChatDetail
import com.fruits.domain.model.chat.SentMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>
    suspend fun refreshChats(accessToken: String)
    suspend fun getChat(accessToken: String, chatId: String): Result<ChatDetail>
    suspend fun deleteChat(accessToken: String, chatId: String): Result<Unit>
    suspend fun sendMessage(accessToken: String, chatId: String, text: String): Result<SentMessage>
}