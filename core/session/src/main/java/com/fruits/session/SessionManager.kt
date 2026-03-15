package com.fruits.session

import android.util.Log
import com.fruits.domain.model.SessionState
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val loginUseCase: LoginUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val tokenRepository: TokenRepository,
    private val userLocalRepository: UserLocalRepository,
    private val userNetworkRepository: UserNetworkRepository
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var previousState: SessionState = SessionState.Loading

    fun toggleDebug() {
        if (_sessionState.value is SessionState.Debug) {
            _sessionState.value = previousState
        } else {
            previousState = _sessionState.value
            _sessionState.value = SessionState.Debug
        }
    }
    suspend fun restoreSession() {
        _sessionState.value = SessionState.Loading

        val cachedUser = userLocalRepository.getCachedUser()
        val refreshToken = tokenRepository.getRefreshToken()

        if (cachedUser == null || refreshToken.isEmpty()) {
            clearSession()
            _sessionState.value = SessionState.Unauthorized()
            return
        }

        userNetworkRepository.refreshToken(refreshToken)
            .onSuccess { tokens ->
                tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken )

                _sessionState.value = if (cachedUser.readyToGive) {
                    SessionState.Authorized
                } else {
                    SessionState.Onboarding()
                }
            }
            .onFailure {
                clearSession()
                _sessionState.value = SessionState.Unauthorized()
            }
    }


    suspend fun login(email: String, password: String): Result<Unit> {
        _sessionState.value = SessionState.Loading
        return loginUseCase(email, password)
            .onSuccess {
                Log.d("SessionManager", "Login success")
                if (it.readyToGive) {
                    _sessionState.value = SessionState.Authorized
                } else {
                    _sessionState.value = SessionState.Onboarding()
                }
            }
            .onFailure {
                Log.d("SessionManager", "Login failed $it")
                _sessionState.value = SessionState.Unauthorized(it.message)
            }
            .map { }
    }

    suspend fun onboard(
        description: String,
        imageIds: List<String>
    ): Result<Unit> {
        _sessionState.value = SessionState.Loading
        return updateProfileUseCase(
            description,
            imageIds
        )
            .onSuccess {
                if (it.readyToGive) {
                    _sessionState.value = SessionState.Authorized
                } else {
                    _sessionState.value = SessionState.Onboarding()
                }
            }
            .onFailure {
                _sessionState.value = SessionState.Onboarding(it.message)
            }
            .map { }
    }

    suspend fun logout() {
        clearSession()
        _sessionState.value = SessionState.Unauthorized()
    }

    private suspend fun clearSession() {
        tokenRepository.clear()
        userLocalRepository.clearCache()
    }
}

