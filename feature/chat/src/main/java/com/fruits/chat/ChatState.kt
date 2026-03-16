package com.fruits.chat

import com.fruits.domain.model.interactions.ReportReason

data class ChatMessage(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val timestampMillis: Long = 0L,
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val chatTitle: String = "Чат",
    val counterpartAvatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val targetUserId: String = "",
    val isBlocked: Boolean = false,
    val isBlockLoading: Boolean = false,
    val blockError: String? = null,
    val isReportLoading: Boolean = false,
    val reportError: String? = null,
    val showReportDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val isPollingError: Boolean = false,
    val isChatDeleted: Boolean = false,
)

sealed interface ChatEvent {
    data class InputChanged(val value: String) : ChatEvent
    data object SendClicked : ChatEvent
    data object BlockUserClicked : ChatEvent
    data object UnblockUserClicked : ChatEvent
    data object ReportUserClicked : ChatEvent
    data class ReportReasonSelected(val reason: ReportReason, val comment: String?) : ChatEvent
    data object DismissReportDialog : ChatEvent
    data object DismissSnackbar : ChatEvent
    data object DeleteChatClicked : ChatEvent
}
