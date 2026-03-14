package com.fruits.domain.model.user

data class UserProfileUpdate(
    val description: String,
    val photoFilesKeys: List<String>
)