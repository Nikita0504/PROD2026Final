package com.fruits.network.user.schema

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginSchema(
    val email: String,
    val password: String
)