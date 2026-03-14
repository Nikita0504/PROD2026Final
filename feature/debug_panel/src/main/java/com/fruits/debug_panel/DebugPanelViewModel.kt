package com.fruits.debug_panel

import androidx.lifecycle.ViewModel
import com.fruits.debug.DebugLogStorage
import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.Files.copy

class DebugPanelViewModel(
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService
) : ViewModel() {

    val logs = DebugLogStorage.logs
    fun clearLogs() = DebugLogStorage.clear()

    val mockEnabled = mockStorage.enabledFlow
    fun toggleMocks(enabled: Boolean) { mockStorage.enabled = enabled }

    private val _userMockState = MutableStateFlow(mockDataService.userMock.toEditState())
    val userMockState: StateFlow<UserMockEditState> = _userMockState.asStateFlow()

    fun updateUserMockState(state: UserMockEditState) { _userMockState.value = state }

    fun saveUserMock() {
        mockDataService.updateUser {
            copy(
                firstName = _userMockState.value.firstName,
                secondName = _userMockState.value.secondName,
                email = _userMockState.value.email
            )
        }
    }
}

data class UserMockEditState(
    val firstName: String,
    val secondName: String,
    val email: String
)

fun User.toEditState() = UserMockEditState(
    firstName = firstName,
    secondName = secondName,
    email = email
)
