package com.fruits.domain.model.user

data class User(
    val id: Int,
    val firstName: String,
    val secondName: String,
    val email: String,
    val avatarFileKey: String?
)
