package com.fruits.session

import com.fruits.domain.model.SessionState
import com.fruits.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val authRepository: AuthRepository
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    suspend fun restoreSession() {
        _sessionState.value = SessionState.Loading

        _sessionState.value = try {
            authRepository.getActiveSession()
            SessionState.Authorized
        } catch (e: Exception) {
            SessionState.Unauthorized
        }
    }

    fun markAuthorized() { _sessionState.value = SessionState.Authorized }
    fun markUnauthorized() { _sessionState.value = SessionState.Unauthorized }
}

