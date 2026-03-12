package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.User
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository

class RegisterUserUseCase(
    private val userNetworkRepository: UserNetworkRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenRepository: TokenRepository
) {
    suspend operator fun invoke(
        firstName: String,
        secondName: String,
        email: String,
        password: String
    ): Result<User> {
        val auth = userNetworkRepository.register(firstName, secondName, email, password)
            .getOrElse { return Result.failure(it) }

        tokenRepository.saveTokens(auth.tokens.accessToken, auth.tokens.refreshToken)
        userLocalRepository.upsertUser(auth.user)

        return Result.success(auth.user)
    }
}


