package com.fruits.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fruits.domain.model.chat.ChatDetail
import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.usecase.chat.DeleteChatUseCase
import com.fruits.domain.usecase.chat.GetChatUseCase
import com.fruits.domain.usecase.chat.SendChatMessageUseCase
import com.fruits.domain.usecase.interactions.ReportUserUseCase
import com.fruits.domain.usecase.interactions.SendUserActionUseCase
import com.fruits.logger.Log
import com.fruits.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import android.util.Base64
import org.json.JSONObject

private const val POLLING_INTERVAL_MS = 4_000L

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChatUseCase: GetChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val sendUserActionUseCase: SendUserActionUseCase,
    private val reportUserUseCase: ReportUserUseCase,
    private val tokenRepository: TokenRepository,
    private val imageUploadUrlRepository: ImageUploadUrlRepository,
) : ViewModel() {

    val chatId: String = savedStateHandle.toRoute<Route.Chat>().chatId
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var currentUserId: String = ""
    private var pollingJob: Job? = null
    private var isUpdatingMessages = false

    init {
        currentUserId = extractUserIdFromJwt(tokenRepository.getAccessToken())
        Log.d(TAG, "Resolved currentUserId='$currentUserId' from JWT")
        viewModelScope.launch {
            loadChat(isInitial = true)
            startPolling()
        }
    }

    private fun extractUserIdFromJwt(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return ""
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING), Charsets.UTF_8)
            JSONObject(payloadJson).getString("sub")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract userId from JWT: ${e.message}")
            ""
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> _state.update { it.copy(inputText = event.value) }
            ChatEvent.SendClicked -> sendMessage()
            ChatEvent.BlockUserClicked -> blockUser()
            ChatEvent.UnblockUserClicked -> unblockUser()
            ChatEvent.ReportUserClicked -> _state.update { it.copy(showReportDialog = true) }
            is ChatEvent.ReportReasonSelected -> reportUser(event.reason, event.comment)
            ChatEvent.DismissReportDialog -> _state.update { it.copy(showReportDialog = false) }
            ChatEvent.DismissSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            ChatEvent.DeleteChatClicked -> deleteChat()
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLLING_INTERVAL_MS)
                if (!isUpdatingMessages && !_state.value.isBlocked) {
                    refreshMessages()
                }
            }
        }
    }

    private suspend fun loadChat(isInitial: Boolean = false) {
        if (isInitial) {
            _state.update { it.copy(isLoading = true, error = null) }
        }

        getChatUseCase(chatId)
            .onSuccess { detail ->
                val avatarUrl = resolveAvatarUrl(detail)
                val messages = mapMessages(detail)
                val isBlocked = detail.status.equals("BLOCKED", ignoreCase = true)
                _state.update {
                    it.copy(
                        chatTitle = detail.title,
                        messages = messages,
                        targetUserId = detail.counterpartUserId,
                        counterpartAvatarUrl = avatarUrl,
                        isBlocked = isBlocked,
                        isLoading = false,
                        error = null,
                        isPollingError = false,
                    )
                }
            }
            .onFailure { error ->
                Log.w(TAG, "Failed to load chat $chatId: ${error.message}")
                if (isInitial) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось загрузить чат",
                        )
                    }
                } else {
                    _state.update { it.copy(isPollingError = true) }
                }
            }
    }

    private suspend fun refreshMessages() {
        if (isUpdatingMessages) return
        isUpdatingMessages = true
        try {
            getChatUseCase(chatId)
                .onSuccess { detail ->
                    val newMessages = mapMessages(detail)
                    val currentIds = _state.value.messages
                        .filter { it.id.startsWith("local_") }
                        .map { it.id }
                        .toSet()
                    val merged = mergeMessages(_state.value.messages, newMessages, currentIds)
                    val isBlocked = detail.status.equals("BLOCKED", ignoreCase = true)
                    _state.update { it.copy(messages = merged, isBlocked = isBlocked, isPollingError = false) }
                }
                .onFailure {
                    _state.update { it.copy(isPollingError = true) }
                }
        } finally {
            isUpdatingMessages = false
        }
    }

    private fun mergeMessages(
        current: List<ChatMessage>,
        fresh: List<ChatMessage>,
        localIds: Set<String>,
    ): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        result.addAll(fresh)
        val locals = current.filter { it.id in localIds }
        val matchedFresh = BooleanArray(fresh.size)
        locals.forEach { local ->
            val idx = fresh.indices.indexOfFirst { i ->
                !matchedFresh[i] && fresh[i].isOwn && fresh[i].text == local.text
            }
            if (idx == -1) {
                result.add(local)
            } else {
                matchedFresh[idx] = true
            }
        }
        return result.sortedBy { it.timestampMillis }
    }

    private suspend fun resolveAvatarUrl(detail: ChatDetail): String? {
        val key = detail.counterpartAvatarKey ?: return null
        return imageUploadUrlRepository.getDownloadUrls(listOf(key))
            .onFailure { Log.w(TAG, "Failed to get avatar URL for $key: ${it.message}") }
            .getOrNull()
            ?.firstOrNull()
            ?.url
    }

    private fun mapMessages(detail: ChatDetail): List<ChatMessage> {
        return detail.messages.map { msg ->
            ChatMessage(
                id = msg.id,
                text = msg.text,
                isOwn = msg.senderUserId == currentUserId,
                timestampMillis = parseIsoTimestamp(msg.createdAt),
            )
        }.sortedBy { it.timestampMillis }
    }

    private fun parseIsoTimestamp(iso: String): Long {
        return try {
            Instant.parse(iso).toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isBlocked) return

        viewModelScope.launch {
            val localId = "local_${UUID.randomUUID()}"
            val localMessage = ChatMessage(
                id = localId,
                text = text,
                isOwn = true,
                timestampMillis = System.currentTimeMillis(),
            )
            _state.update {
                it.copy(messages = it.messages + localMessage, inputText = "")
            }

            sendChatMessageUseCase(chatId, text)
                .onSuccess {
                    refreshMessages()
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to send message: ${error.message}")
                    _state.update {
                        it.copy(
                            messages = it.messages.filterNot { m -> m.id == localId },
                            inputText = text,
                            error = error.message ?: "Не удалось отправить сообщение",
                        )
                    }
                }
        }
    }

    private fun blockUser() {
        val targetId = _state.value.targetUserId
        if (targetId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isBlockLoading = true, blockError = null) }
            sendUserActionUseCase(targetId, UserAction.BLOCK)
                .onSuccess {
                    Log.i(TAG, "User $targetId blocked successfully")
                    _state.update {
                        it.copy(
                            isBlockLoading = false,
                            isBlocked = true,
                            snackbarMessage = "Пользователь заблокирован",
                        )
                    }
                    pollingJob?.cancel()
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to block user $targetId: ${error.message}")
                    _state.update {
                        it.copy(
                            isBlockLoading = false,
                            blockError = error.message ?: "Не удалось заблокировать пользователя",
                            snackbarMessage = error.message ?: "Не удалось заблокировать пользователя",
                        )
                    }
                }
        }
    }

    private fun unblockUser() {
        val targetId = _state.value.targetUserId
        if (targetId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isBlockLoading = true, blockError = null) }
            sendUserActionUseCase(targetId, UserAction.UNBLOCK)
                .onSuccess {
                    Log.i(TAG, "User $targetId unblocked successfully")
                    _state.update {
                        it.copy(
                            isBlockLoading = false,
                            isBlocked = false,
                            snackbarMessage = "Пользователь разблокирован",
                        )
                    }
                    startPolling()
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to unblock user $targetId: ${error.message}")
                    _state.update {
                        it.copy(
                            isBlockLoading = false,
                            blockError = error.message ?: "Не удалось разблокировать пользователя",
                            snackbarMessage = error.message ?: "Не удалось разблокировать пользователя",
                        )
                    }
                }
        }
    }

    private fun reportUser(reason: ReportReason, comment: String?) {
        val targetId = _state.value.targetUserId
        if (targetId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isReportLoading = true, reportError = null, showReportDialog = false) }
            reportUserUseCase(targetId, reason, comment)
                .onSuccess {
                    Log.i(TAG, "User $targetId reported: $reason")
                    _state.update {
                        it.copy(
                            isReportLoading = false,
                            snackbarMessage = "Жалоба отправлена",
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to report user $targetId: ${error.message}")
                    _state.update {
                        it.copy(
                            isReportLoading = false,
                            reportError = error.message ?: "Не удалось отправить жалобу",
                            snackbarMessage = error.message ?: "Не удалось отправить жалобу",
                        )
                    }
                }
        }
    }

    private fun deleteChat() {
        viewModelScope.launch {
            pollingJob?.cancel()
            deleteChatUseCase(chatId)
                .onSuccess {
                    Log.i(TAG, "Chat $chatId deleted successfully")
                    _state.update { it.copy(isChatDeleted = true) }
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to delete chat $chatId: ${error.message}")
                    _state.update {
                        it.copy(
                            snackbarMessage = error.message ?: "Не удалось удалить чат",
                        )
                    }
                    startPolling()
                }
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
