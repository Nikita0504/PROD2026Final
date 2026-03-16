package com.fruits.profile

import com.fruits.domain.model.user.User

data class ProfileState(
    val user: User? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
sealed interface ProfileEvent {
    data object Refresh : ProfileEvent
    data object Logout : ProfileEvent
}

sealed interface ProfileEffect {
    data class ShowErrorSnackbar(val message: String) : ProfileEffect
}