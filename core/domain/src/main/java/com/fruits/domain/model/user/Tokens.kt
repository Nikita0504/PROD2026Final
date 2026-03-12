package com.fruits.domain.model.user

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)