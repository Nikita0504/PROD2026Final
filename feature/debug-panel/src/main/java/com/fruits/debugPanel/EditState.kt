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
    val id: String,
    val authorName: String,
    val description: String,
    val likesCount: String,
    val isLiked: Boolean
)

