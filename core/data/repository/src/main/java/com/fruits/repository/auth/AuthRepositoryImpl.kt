package com.fruits.repository.auth

import com.fruits.domain.model.AuthToken
import com.fruits.domain.repository.AuthRepository

class AuthRepositoryImpl(): AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): AuthToken {
        return AuthToken(
            accessToken = "dsd",
            refreshToken = "dsd"
        )
    }

    override suspend fun refresh(refreshToken: String): AuthToken {
        return AuthToken(
            accessToken = "dsd",
            refreshToken = "dsd"
        )    }

    override suspend fun getActiveSession(): AuthToken {
        throw NoSuchElementException("No active session")
    }

    override suspend fun clearSession() {}
}