package com.fruits.debugPanel

import androidx.lifecycle.ViewModel
import com.fruits.debug.DebugMockData
import com.fruits.debug.MockStorage
import com.fruits.debugPanel.extension.toEditState
import com.fruits.logger.DebugLogStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DebugPanelViewModel(
    private val mockStorage: MockStorage,
    private val mockDataService: DebugMockData
) : ViewModel() {

    val logs = DebugLogStorage.logs
    val mockEnabled = mockStorage.enabledFlow

    fun toggleMocks(enabled: Boolean) { mockStorage.enabled = enabled }
    fun clearLogs() = DebugLogStorage.clear()

    // region User

    private val _userState = MutableStateFlow(mockDataService.userMock.toEditState())
    val userState: StateFlow<UserMockEditState> = _userState.asStateFlow()

    fun updateUserState(state: UserMockEditState) { _userState.value = state }

    fun saveUserMock() {
        mockDataService.updateUser {
            copy(
                firstName = _userState.value.firstName,
                secondName = _userState.value.secondName,
                email = _userState.value.email
            )
        }
    }

    // endregion

    // region Tokens

    private val _tokensState = MutableStateFlow(mockDataService.tokensMock.toEditState())
    val tokensState: StateFlow<TokensMockEditState> = _tokensState.asStateFlow()

    fun updateTokensState(state: TokensMockEditState) { _tokensState.value = state }

    fun saveTokensMock() {
        mockDataService.updateTokens {
            copy(
                accessToken = _tokensState.value.accessToken,
                refreshToken = _tokensState.value.refreshToken
            )
        }
    }

    // endregion

    // region Recommendations

    private val _recommendationsState = MutableStateFlow(
        mockDataService.recommendations.toEditState()
    )
    val recommendationsState: StateFlow<List<RecommendationMockEditState>> =
        _recommendationsState.asStateFlow()

    fun updateRecommendationState(updated: RecommendationMockEditState) {
        _recommendationsState.value = _recommendationsState.value.map {
            if (it.userId == updated.userId) updated else it
        }
    }

    fun saveRecommendationsMock() {
        mockDataService.updateRecommendations {
            val states = _recommendationsState.value
            replaceAll { rec ->
                val state = states.find { it.userId == rec.userId } ?: return@replaceAll rec
                rec.copy(
                    firstName = state.firstName,
                    secondName = state.secondName,
                    age = state.age.toIntOrNull() ?: rec.age,
                    city = state.city,
                    description = state.description,
                    explanation = state.explanation.lines().filter { it.isNotBlank() }
                )
            }
        }
    }

    // endregion

    // region Chats

    private val _chatsState = MutableStateFlow(mockDataService.chats.toEditState())
    val chatsState: StateFlow<List<ChatMockEditState>> = _chatsState.asStateFlow()

    fun updateChatState(updated: ChatMockEditState) {
        _chatsState.value = _chatsState.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun saveChatsMock() {
        val states = _chatsState.value
        mockDataService.chats = mockDataService.chats.map { chat ->
            val state = states.find { it.id == chat.id } ?: return@map chat
            chat.copy(
                name = state.name,
                lastMessage = state.lastMessage,
                unreadCount = state.unreadCount.toIntOrNull() ?: chat.unreadCount,
            )
        }
    }

    // endregion
}