package com.fruits.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeAuthViewModel

    @Before
    fun setup() {
        fakeViewModel = FakeAuthViewModel()
    }

    @Test
    fun renderAuthState_initialState_showsTitleAndInputFields() {
        // Given
        val initialState = AuthState()

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = initialState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("auth_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun renderAuthState_loadingState_showsProgressIndicator() {
        // Given
        val loadingState = AuthState(
            email = "test@example.com",
            password = "password123",
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = loadingState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("login_progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
        // Corner Case: Fields should be disabled during loading
        composeTestRule.onNodeWithTag("email_field").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("password_field").assertIsNotEnabled()
    }

    @Test
    fun renderAuthState_errorState_showsErrorMessage() {
        // Given
        val errorState = AuthState(
            email = "wrong@example.com",
            password = "wrong",
            errorMessage = "Неверный email или пароль"
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = errorState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_message").assertTextContains("Неверный email или пароль")
    }

    @Test
    fun renderAuthState_canSubmitTrue_enablesLoginButton() {
        // Given
        val validState = AuthState(
            email = "test@example.com",
            password = "password123",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = validState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun renderAuthState_canSubmitFalse_disablesLoginButton() {
        // Given
        val invalidState = AuthState(
            email = "",
            password = "",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = invalidState,
                onEvent = fakeViewModel::onEvent
            )
        }

        // Then
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }

    @Test
    fun inputEmail_emitsEmailChangedEvent() {
        // Given
        val initialState = AuthState()

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = initialState,
                onEvent = fakeViewModel::onEvent
            )
        }

        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")

        // Then
        assertThat(fakeViewModel.lastEmail).isEqualTo("test@example.com")
    }

    @Test
    fun inputPassword_emitsPasswordChangedEvent() {
        // Given
        val initialState = AuthState()

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = initialState,
                onEvent = fakeViewModel::onEvent
            )
        }

        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")

        // Then
        assertThat(fakeViewModel.lastPassword).isEqualTo("password123")
    }

    @Test
    fun clickLoginButton_emitsLoginClickedEvent() {
        // Given
        val validState = AuthState(
            email = "test@example.com",
            password = "password123",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = validState,
                onEvent = fakeViewModel::onEvent
            )
        }

        composeTestRule.onNodeWithTag("login_button").performClick()

        // Then
        assertThat(fakeViewModel.loginClicked).isTrue()
    }

    @Test
    fun clickLoginButton_whenLoading_doesNotEmitEvent() {
        // Given
        val loadingState = AuthState(
            email = "test@example.com",
            password = "password123",
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            AuthScreen(
                state = loadingState,
                onEvent = fakeViewModel::onEvent
            )
        }

        composeTestRule.onNodeWithTag("login_button").performClick()

        // Then
        assertThat(fakeViewModel.loginClicked).isFalse()
    }

    @Test
    fun longErrorMessage_isDisplayedCorrectly() {
        // Corner Case: Very long error message
        val longError = "A very long error message that might potentially break the UI layout if not handled correctly by the Compose components. Let's see how it behaves."
        val state = AuthState(errorMessage = longError)

        composeTestRule.setContent {
            AuthScreen(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithTag("error_message")
            .assertIsDisplayed()
            .assertTextContains(longError)
    }

    @Test
    fun emailField_whenDisabled_cannotBeInteractedWith() {
        // Corner Case: Interacting with fields when loading
        val state = AuthState(isLoading = true)

        composeTestRule.setContent {
            AuthScreen(state = state, onEvent = fakeViewModel::onEvent)
        }

        composeTestRule.onNodeWithTag("email_field").assertIsNotEnabled()
        // Note: performTextInput might still "work" in some test environments even if enabled=false, 
        // but assertIsNotEnabled is the correct check for UI state.
    }
}

// Fake ViewModel для тестирования
class FakeAuthViewModel {
    var lastEmail: String? = null
    var lastPassword: String? = null
    var loginClicked: Boolean = false

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                lastEmail = event.value
            }
            is AuthEvent.PasswordChanged -> {
                lastPassword = event.value
            }
            AuthEvent.LoginClicked -> {
                loginClicked = true
            }
        }
    }
}
