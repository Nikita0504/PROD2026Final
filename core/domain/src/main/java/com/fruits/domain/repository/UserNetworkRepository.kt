package com.fruits.domain.repository

import com.fruits.domain.model.user.AuthResult
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User

interface UserNetworkRepository {

    suspend fun register(
        firstName: String,
        secondName: String,
        email: String,
        password: String
    ): Result<AuthResult>

    suspend fun login(email: String, password: String): Result<Tokens>
    suspend fun getProfile(accessToken: String): Result<User>

    suspend fun refreshToken(refreshToken: String): Result<Tokens>
}
