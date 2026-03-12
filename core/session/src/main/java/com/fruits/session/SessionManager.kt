package com.fruits.session

import com.fruits.domain.model.SessionState
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUserUseCase,
    private val tokenRepository: TokenRepository,
    private val userLocalRepository: UserLocalRepository
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    suspend fun restoreSession() {
        _sessionState.value = SessionState.Loading
        val cachedUser = userLocalRepository.getCachedUser()
        val hasToken = tokenRepository.getAccessToken().isNotEmpty()

        _sessionState.value = if (cachedUser != null && hasToken) {
            SessionState.Authorized
        } else {
            if (cachedUser != null || hasToken) clearSession()
            SessionState.Unauthorized
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        _sessionState.value = SessionState.Loading
        return loginUseCase(email, password)
            .onSuccess { _sessionState.value = SessionState.Authorized }
            .onFailure { _sessionState.value = SessionState.Unauthorized }
            .map { Unit }
    }

    suspend fun register(
        firstName: String,
        secondName: String,
        email: String,
        password: String
    ): Result<Unit> {
        _sessionState.value = SessionState.Loading
        return registerUseCase(firstName, secondName, email, password)
            .onSuccess { _sessionState.value = SessionState.Authorized }
            .onFailure { _sessionState.value = SessionState.Unauthorized }
            .map { Unit }
    }

    suspend fun logout() {
        clearSession()
        _sessionState.value = SessionState.Unauthorized
    }

    private suspend fun clearSession() {
        tokenRepository.clear()
        userLocalRepository.clearCache()
    }
}

