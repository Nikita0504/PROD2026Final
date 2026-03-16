package com.fruits.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data object Authorized : SessionState
    data object Onboarding : SessionState
    data object Unauthorized : SessionState

    data object Debug : SessionState

}
