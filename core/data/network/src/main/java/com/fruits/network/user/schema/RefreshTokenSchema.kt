package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenSchema(
    @SerialName("refresh_token") val refreshToken: String
)