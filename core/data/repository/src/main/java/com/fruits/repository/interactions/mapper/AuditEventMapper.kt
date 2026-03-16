package com.fruits.repository.interactions.mapper

import com.fruits.domain.model.interactions.AuditEvent
import com.fruits.network.interactions.schema.AuditEventReadSchema

fun AuditEventReadSchema.toAuditEvent(): AuditEvent = AuditEvent(
    eventType = eventType,
    title = title,
    description = description,
    createdAt = createdAt,
)
