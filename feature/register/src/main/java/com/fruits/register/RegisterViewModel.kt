package com.fruits.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FirstNameChanged ->
                _state.update { it.copy(firstName = event.value, errorMessage = null) }

            is RegisterEvent.SecondNameChanged ->
                _state.update { it.copy(secondName = event.value, errorMessage = null) }

            is RegisterEvent.EmailChanged ->
                _state.update { it.copy(email = event.value, errorMessage = null) }

            is RegisterEvent.PasswordChanged ->
                _state.update { it.copy(password = event.value, errorMessage = null) }

            is RegisterEvent.RepeatPasswordChanged ->
                _state.update { it.copy(repeatPassword = event.value, errorMessage = null) }

            RegisterEvent.RegisterClicked -> onRegisterClicked()
        }
    }

    private fun onRegisterClicked() {
        val current = _state.value
        if (!current.canSubmit || current.isLoading) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = sessionManager.register(
                firstName = current.firstName,
                secondName = current.secondName,
                email = current.email,
                password = current.password,
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}

