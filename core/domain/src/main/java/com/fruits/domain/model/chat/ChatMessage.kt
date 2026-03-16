package com.fruits.domain.model.chat

data class ChatMessage(
    val id: String,
    val senderUserId: String,
    val text: String,
    val createdAt: String,
)

