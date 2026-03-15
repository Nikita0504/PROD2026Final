package com.fruits.repository.token

import com.fruits.database.token.TokenStorage
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TokenRepositoryImplTest {

    @MockK
    private lateinit var storage: TokenStorage

    private lateinit var repository: TokenRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = TokenRepositoryImpl(storage)
    }

    @Test
    fun `getAccessToken returns token from storage`() {
        // Given
        every { storage.getAccessToken() } returns "access_token_123"

        // When
        val result = repository.getAccessToken()

        // Then
        assertEquals("access_token_123", result)
    }

    @Test
    fun `getAccessToken returns empty string when storage returns empty`() {
        // Given
        every { storage.getAccessToken() } returns ""

        // When
        val result = repository.getAccessToken()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `getRefreshToken returns token from storage`() {
        // Given
        every { storage.getRefreshToken() } returns "refresh_token_456"

        // When
        val result = repository.getRefreshToken()

        // Then
        assertEquals("refresh_token_456", result)
    }

    @Test
    fun `getRefreshToken returns empty string when storage returns empty`() {
        // Given
        every { storage.getRefreshToken() } returns ""

        // When
        val result = repository.getRefreshToken()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `saveTokens calls storage with correct parameters`() = runTest {
        // Given
        val accessToken = "new_access_token"
        val refreshToken = "new_refresh_token"
        // Используем coEvery для suspend функций
        coEvery { storage.saveTokens(any(), any()) } returns Unit

        // When
        repository.saveTokens(accessToken, refreshToken)

        // Then
        // Используем coVerify для проверки вызова suspend функций
        coVerify { storage.saveTokens(accessToken, refreshToken) }
    }

    @Test
    fun `clear calls storage clearTokens`() = runTest {
        // Given
        coEvery { storage.clearTokens() } returns Unit

        // When
        repository.clear()

        // Then
        coVerify { storage.clearTokens() }
    }
}