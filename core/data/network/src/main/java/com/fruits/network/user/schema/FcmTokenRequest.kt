package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    @SerialName("fcm_token")
    val fcmToken: String
)
