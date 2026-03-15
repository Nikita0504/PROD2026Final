package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginSchema(
    val email: String,
    val password: String,
    @SerialName("android_push_token")
    val androidPushToken: String? = null
)