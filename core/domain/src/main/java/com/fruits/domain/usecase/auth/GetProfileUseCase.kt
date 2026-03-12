package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.User
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProfileUseCase(
    private val userNetworkRepository: UserNetworkRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenRepository: TokenRepository
) {
    operator fun invoke(): Flow<Result<User>> = flow {
        val cached = userLocalRepository.getCachedUser()
        if (cached != null) emit(Result.success(cached))

        val token = tokenRepository.getAccessToken()
        if (token.isEmpty()) {
            if (cached == null) emit(Result.failure(IllegalStateException("Не авторизован")))
            return@flow
        }

        userNetworkRepository.getProfile(token)
            .onSuccess { user ->
                userLocalRepository.upsertUser(user)
                emit(Result.success(user))
            }
            .onFailure { e ->
                if (cached == null) emit(Result.failure(e))
            }
    }
}
