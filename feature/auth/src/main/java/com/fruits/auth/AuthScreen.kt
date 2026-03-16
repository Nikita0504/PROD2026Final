package com.fruits.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
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
            Text(
                text = "Вход",
                modifier = Modifier.testTag("auth_title"),
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .testTag("email_field"),
                value = state.email,
                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("password_field"),
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
                    .padding(top = 24.dp)
                    .testTag("login_button"),
                onClick = { onEvent(AuthEvent.LoginClicked) },
                enabled = state.canSubmit,
            ) {
                Text("Войти")
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .testTag("login_progress"),
                    strokeWidth = 2.dp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .testTag("error_message"),
                )
            }
        }
    }
}
