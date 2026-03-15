package com.fruits.network.interactions.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserReportCreateSchema(
    @SerialName("target_user_id")
    val targetUserId: String,
    val reason: String,
    val comment: String? = null,
)

