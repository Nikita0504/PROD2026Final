package com.fruits.profile

data class ProfileState(
    val avatarUrl: String = "",
    val name: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
)


