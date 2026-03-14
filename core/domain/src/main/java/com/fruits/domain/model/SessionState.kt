package com.fruits.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data object Authorized : SessionState
    data class Onboarding(val error: String? = null) : SessionState
    data class Unauthorized(val error: String? = null) : SessionState

    data object Debug : SessionState

}
