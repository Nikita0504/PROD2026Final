package com.fruits.network.interactions.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuditEventReadSchema(
    @SerialName("event_type")
    val eventType: String,
    val title: String,
    val description: String,
    @SerialName("created_at")
    val createdAt: String,
)
