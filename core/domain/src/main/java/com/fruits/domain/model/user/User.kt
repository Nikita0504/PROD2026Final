package com.fruits.domain.model.user

data class User(
    val id: String,
    val firstName: String,
    val secondName: String,
    val email: String,
    val avatarFileKey: String?,
    val readyToGive: Boolean,
    val description: String? = null,
    val photoFileKeys: List<String> = listOf()
)
