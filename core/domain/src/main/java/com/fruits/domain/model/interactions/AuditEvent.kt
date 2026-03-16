package com.fruits.domain.model.interactions

data class AuditEvent(
    val eventType: String,
    val title: String,
    val description: String,
    val createdAt: String,
)
