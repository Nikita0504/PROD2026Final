package com.fruits.debugPanel


import androidx.lifecycle.ViewModel
import com.fruits.debug.DebugLogStorage
import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.debugPanel.extension.toEditState
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DebugPanelViewModel(
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService
) : ViewModel() {

    val logs = DebugLogStorage.logs
    val mockEnabled = mockStorage.enabledFlow

    fun toggleMocks(enabled: Boolean) { mockStorage.enabled = enabled }
    fun clearLogs() = DebugLogStorage.clear()

    // User
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

    private val _recommendationsState = MutableStateFlow(
        mockDataService.recommendations.toEditState()
    )
    val recommendationsState: StateFlow<List<RecommendationMockEditState>> =
        _recommendationsState.asStateFlow()

    fun updateRecommendationState(updated: RecommendationMockEditState) {
        _recommendationsState.value = _recommendationsState.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun saveRecommendationsMock() {
        mockDataService.updateRecommendations {
            val states = _recommendationsState.value
            replaceAll { rec ->
                val state = states.find { it.id == rec.id } ?: return@replaceAll rec
                rec.copy(
                    authorName = state.authorName,
                    description = state.description,
                    likesCount = state.likesCount.toIntOrNull() ?: rec.likesCount,
                    isLiked = state.isLiked
                )
            }
        }
    }
}