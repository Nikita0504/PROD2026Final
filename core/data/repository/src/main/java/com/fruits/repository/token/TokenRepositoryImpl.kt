package com.fruits.repository.token

import com.fruits.database.token.TokenStorage
import com.fruits.domain.repository.TokenRepository

class TokenRepositoryImpl(
    private val storage: TokenStorage
) : TokenRepository {

    override fun getAccessToken(): String = storage.getAccessToken()

    override fun getRefreshToken(): String = storage.getRefreshToken()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) =
        storage.saveTokens(accessToken, refreshToken)

    override suspend fun clear() = storage.clearTokens()
}