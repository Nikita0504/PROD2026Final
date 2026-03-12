package com.fruits.domain.repository

interface TokenRepository {
    fun getAccessToken(): String

    fun getRefreshToken(): String

    suspend fun saveTokens(accessToken: String, refreshToken: String)

    suspend fun clear()
}