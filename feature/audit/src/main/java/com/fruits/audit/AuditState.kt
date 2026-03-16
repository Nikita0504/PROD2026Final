package com.fruits.audit

import com.fruits.domain.model.interactions.AuditEvent

data class AuditState(
    val events: List<AuditEvent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuditIntent {
    data object Refresh : AuditIntent
}

sealed interface AuditEffect {
    data class ShowErrorSnackbar(val message: String) : AuditEffect
}
