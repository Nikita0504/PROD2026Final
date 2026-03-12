package com.fruits.register

data class RegisterState(
    val firstName: String = "",
    val secondName: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = firstName.isNotBlank() &&
            secondName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            repeatPassword == password
}

sealed interface RegisterEvent {
    data class FirstNameChanged(val value: String) : RegisterEvent
    data class SecondNameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class RepeatPasswordChanged(val value: String) : RegisterEvent
    data object RegisterClicked : RegisterEvent
}


