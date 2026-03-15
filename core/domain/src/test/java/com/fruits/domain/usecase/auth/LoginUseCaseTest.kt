package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {

    @MockK
    private lateinit var userNetworkRepository: UserNetworkRepository

    @MockK
    private lateinit var userLocalRepository: UserLocalRepository

    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        loginUseCase = LoginUseCase(userNetworkRepository, userLocalRepository, tokenRepository)
    }

    @Test
    fun `invoke with valid credentials returns success with user`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val tokens = Tokens(accessToken = "access_token", refreshToken = "refresh_token")
        val user = mockk<User>(relaxed = true)

        coEvery { userNetworkRepository.login(email, password) } returns Result.success(tokens)
        coEvery { tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken) } returns Unit
        coEvery { userNetworkRepository.getProfile(tokens.accessToken) } returns Result.success(user)
        coEvery { userLocalRepository.upsertUser(user) } returns Unit

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken) }
        coVerify { userLocalRepository.upsertUser(user) }
    }

    @Test
    fun `invoke with invalid credentials returns error`() = runTest {
        // Given
        val email = "wrong@example.com"
        val password = "wrong_password"
        val error = Exception("Invalid credentials")

        coEvery { userNetworkRepository.login(email, password) } returns Result.failure(error)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify(exactly = 0) { tokenRepository.saveTokens(any(), any()) }
        coVerify(exactly = 0) { userLocalRepository.upsertUser(any()) }
    }

    @Test
    fun `invoke with empty email returns error`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        val error = Exception("Login failed")

        coEvery { userNetworkRepository.login(email, password) } returns Result.failure(error)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke when getProfile fails returns error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val tokens = Tokens(accessToken = "access_token", refreshToken = "refresh_token")
        val error = Exception("Profile fetch failed")

        coEvery { userNetworkRepository.login(email, password) } returns Result.success(tokens)
        coEvery { tokenRepository.saveTokens(any(), any()) } returns Unit
        coEvery { userNetworkRepository.getProfile(any()) } returns Result.failure(error)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify { tokenRepository.saveTokens(any(), any()) }
    }
}
