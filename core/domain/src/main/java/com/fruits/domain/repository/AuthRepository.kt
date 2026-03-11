package com.fruits.domain.repository

import com.fruits.domain.model.AuthToken

interface AuthRepository {

    suspend fun login(username: String, password: String): AuthToken

    suspend fun refresh(refreshToken: String): AuthToken

    suspend fun getActiveSession(): AuthToken

    suspend fun clearSession()
}
