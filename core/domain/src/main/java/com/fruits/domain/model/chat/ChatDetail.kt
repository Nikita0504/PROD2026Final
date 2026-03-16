package com.fruits.domain.model.chat

data class ChatDetail(
    val id: String,
    val title: String,
    val status: String,
    val messages: List<ChatMessage>,
    val createdAt: String,
    val updatedAt: String,
    val counterpartUserId: String = "",
    val counterpartAvatarKey: String? = null,
)

