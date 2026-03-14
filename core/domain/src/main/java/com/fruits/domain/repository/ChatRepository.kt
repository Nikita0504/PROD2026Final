package com.fruits.domain.repository

import com.fruits.domain.model.chat.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>
    suspend fun refreshChats()
}