package com.fruits.domain.model.chat

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int,
    val avatarUrl: String?,
    val avatarFileKey: String? = null,
)