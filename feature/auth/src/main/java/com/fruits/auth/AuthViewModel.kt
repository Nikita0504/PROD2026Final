package com.fruits.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _state.update { it.copy(email = event.value, errorMessage = null) }
            is AuthEvent.PasswordChanged -> _state.update { it.copy(password = event.value, errorMessage = null) }
            AuthEvent.LoginClicked -> onLoginClicked()
        }
    }

    private fun onLoginClicked() {
        val current = _state.value
        if (!current.canSubmit || current.isLoading) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(1000L)
            _state.update { it.copy(isLoading = false, errorMessage = null) }
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}

