package com.fruits.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProfileState(
            isLoading = true,
            error = null,
            user = null
        )
    )
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Refresh -> loadProfile()
            ProfileEvent.Logout -> logout()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                getProfileUseCase().collect { result ->
                    result.onSuccess { user ->
                        _state.value = ProfileState(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                    }

                    result.onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось загрузить профиль"
                        _state.value = ProfileState(
                            user = null,
                            isLoading = false,
                            error = errorMsg
                        )
                        _effect.send(ProfileEffect.ShowErrorSnackbar(errorMsg))
                    }
                }
            } catch (e: Exception) {
                _state.value = ProfileState(
                    user = null,
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}