package com.fruits.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthRoute(
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToRegister = onNavigateToRegister,
    )
}

@Composable
private fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold { p ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Вход")

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                value = state.email,
                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                enabled = !state.isLoading,
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                value = state.password,
                onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                label = { Text("Пароль") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = PasswordVisualTransformation(),
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                onClick = { onEvent(AuthEvent.LoginClicked) },
                enabled = state.canSubmit && !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Войти")
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                onClick = onNavigateToRegister,
                enabled = !state.isLoading,
            ) {
                Text("Создать аккаунт")
            }
        }
    }
}
