package com.fruits.debugPanel

data class UserMockEditState(
    val firstName: String,
    val secondName: String,
    val email: String
)

data class TokensMockEditState(
    val accessToken: String,
    val refreshToken: String
)

data class RecommendationMockEditState(
    val userId: String,
    val firstName: String,
    val secondName: String,
    val age: String,
    val city: String,
    val description: String,
    val explanation: String,
)

data class ChatMockEditState(
    val id: String,
    val name: String,
    val lastMessage: String,
    val unreadCount: String,
)

