package com.fruits.auth

import com.fruits.domain.model.SessionState
import com.fruits.session.SessionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @MockK
    private lateinit var sessionManager: SessionManager

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        val initialState = SessionState.Unauthorized("Invalid credentials")
        every { sessionManager.sessionState } returns MutableStateFlow(initialState)

        viewModel = AuthViewModel(sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has error message from sessionManager`() = runTest {
        // Then
        val state = viewModel.state.value
        assertEquals("Invalid credentials", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onEvent EmailChanged updates email and clears error`() = runTest {
        // When
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals("test@example.com", state.email)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEvent PasswordChanged updates password and clears error`() = runTest {
        // When
        viewModel.onEvent(AuthEvent.PasswordChanged("newpassword"))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals("newpassword", state.password)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEvent LoginClicked with empty email does not call login`() = runTest {
        // Given
        coEvery { sessionManager.login(any(), any()) } returns Result.failure(Exception("Should not be called"))

        // When
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `onEvent LoginClicked with empty password does not call login`() = runTest {
        // Given
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        advanceUntilIdle()
        coEvery { sessionManager.login(any(), any()) } returns Result.failure(Exception("Should not be called"))

        // When
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
    }


    @Test
    fun `onEvent LoginClicked with successful login clears loading`() = runTest {
        // Given
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        advanceUntilIdle()

        coEvery { sessionManager.login("test@example.com", "password123") } returns Result.success(Unit)

        // When
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEvent LoginClicked with failed login sets error`() = runTest {
        // Given
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        advanceUntilIdle()

        coEvery { sessionManager.login("test@example.com", "password123") } returns Result.failure(Exception("Network error"))

        // When
        viewModel.onEvent(AuthEvent.LoginClicked)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `canSubmit is true when email and password are not blank`() = runTest {
        // When
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when email is blank`() = runTest {
        // When
        viewModel.onEvent(AuthEvent.EmailChanged(""))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when password is blank`() = runTest {
        // When
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged(""))
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.canSubmit)
    }
}
