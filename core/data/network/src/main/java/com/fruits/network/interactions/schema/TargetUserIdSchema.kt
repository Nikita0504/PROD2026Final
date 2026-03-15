package com.fruits.network.interactions.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TargetUserIdSchema(
    @SerialName("target_user_id")
    val targetUserId: String,
)
