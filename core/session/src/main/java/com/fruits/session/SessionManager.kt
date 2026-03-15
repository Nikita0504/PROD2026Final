package com.fruits.session

import com.fruits.domain.model.SessionState
import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.fcm.UpdateFcmTokenUseCase
import com.fruits.logger.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SessionManager(
    private val loginUseCase: LoginUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
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
        val result = loginUseCase(email, password)
            .onSuccess { user ->
                Log.d("SessionManager", "Login success")
                if (user.readyToGive) {
                    _sessionState.value = SessionState.Authorized
                } else {
                    _sessionState.value = SessionState.Onboarding()
                }

                // Отправляем FCM токен после успешного логина
                sendFcmToken()
            }
            .onFailure { error ->
                Log.d("SessionManager", "Login failed $error")
                _sessionState.value = SessionState.Unauthorized(error.message)
            }
        return result.map { }
    }

    private suspend fun sendFcmToken() {
        try {
            val fcmToken = fcmTokenProvider.getFcmToken()
            if (fcmToken != null) {
                updateFcmTokenUseCase(fcmToken)
                    .onSuccess {
                        Log.i("SessionManager", "FCM token sent to server successfully")
                    }
                    .onFailure { error ->
                        // Не прерываем работу, просто логируем ошибку
                        Log.w("SessionManager", "Failed to send FCM token to server: ${error.message}")
                        Log.w("SessionManager", "This may be due to Firebase configuration issues. Check: 1) Firebase project setup 2) google-services.json 3) Internet connection")
                    }
            } else {
                Log.w("SessionManager", "FCM token not available - Firebase may not be properly configured")
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Error getting FCM token", e)
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

