package com.fruits.auth

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val emailError: String?
        get() = when {
            email.isEmpty() -> null
            !isValidEmail(email) -> "Некорректный email"
            else -> null
        }

    val canSubmit: Boolean
        get() = isValidEmail(email) && password.isNotBlank() && !isLoading

    private fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return pattern.toRegex().matches(email)
    }
}

sealed interface AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data object LoginClicked : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
}
