package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.User
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository

class LoginUseCase(
    private val userNetworkRepository: UserNetworkRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenRepository: TokenRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        androidPushToken: String? = null
    ): Result<User> {
        val tokens = userNetworkRepository.login(email, password, androidPushToken)
            .getOrElse { return Result.failure(it) }

        tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)

        return userNetworkRepository.getProfile(tokens.accessToken)
            .onSuccess { user -> userLocalRepository.upsertUser(user) }
    }
}

