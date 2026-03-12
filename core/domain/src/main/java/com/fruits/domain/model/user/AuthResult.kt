package com.fruits.domain.model.user
data class AuthResult(
    val user: User,
    val tokens: Tokens
)