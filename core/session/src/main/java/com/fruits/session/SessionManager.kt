package com.fruits.session

import com.fruits.domain.model.SessionState
import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.logger.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SessionManager(
    private val loginUseCase: LoginUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val fcmTokenProvider: FcmTokenProvider,
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

        val refreshToken = tokenRepository.getRefreshToken()
        if (refreshToken.isEmpty()) {
            _sessionState.value = SessionState.Unauthorized()
            return
        }

        userNetworkRepository.refreshToken(refreshToken)
            .onSuccess { tokens ->
                tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)
                val user = userLocalRepository.getCachedUser()
                _sessionState.value = if (user?.readyToGive == true)
                    SessionState.Authorized else SessionState.Onboarding()
            }
            .onFailure { error ->
                val isUnauthorized = error.message?.contains("401") == true
                if (isUnauthorized) {
                    clearSession()
                    _sessionState.value = SessionState.Unauthorized()
                } else {
                    val cached = userLocalRepository.getCachedUser()
                    _sessionState.value = if (cached != null) {
                        if (cached.readyToGive) SessionState.Authorized
                        else SessionState.Onboarding()
                    } else {
                        SessionState.Unauthorized()
                    }
                }
            }
    }




    suspend fun login(email: String, password: String): Result<Unit> {
        _sessionState.value = SessionState.Loading

        // Получаем FCM токен (может быть null если пользователь отказался от уведомлений)
        val fcmToken = getFcmTokenOrNull()

        val result = loginUseCase(email, password, fcmToken)
            .onSuccess { user ->
                Log.d("SessionManager", "Login success")
                if (user.readyToGive) {
                    _sessionState.value = SessionState.Authorized
                } else {
                    _sessionState.value = SessionState.Onboarding()
                }
            }
            .onFailure { error ->
                Log.d("SessionManager", "Login failed $error")
                _sessionState.value = SessionState.Unauthorized(error.message)
            }
        return result.map { }
    }

    private suspend fun getFcmTokenOrNull(): String? {
        return try {
            val token = fcmTokenProvider.getFcmToken()
            if (token != null) {
                Log.d("SessionManager", "FCM token retrieved: ${token.take(20)}...")
            } else {
                Log.w("SessionManager", "FCM token not available - user may have declined notifications or Firebase is not configured")
            }
            token
        } catch (e: Exception) {
            Log.e("SessionManager", "Error getting FCM token", e)
            null
        }
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

