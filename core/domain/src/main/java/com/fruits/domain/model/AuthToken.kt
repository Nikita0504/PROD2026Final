package com.fruits.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
)