package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.User
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProfileUseCaseTest {

    @MockK
    private lateinit var userNetworkRepository: UserNetworkRepository

    @MockK
    private lateinit var userLocalRepository: UserLocalRepository

    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var getProfileUseCase: GetProfileUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getProfileUseCase = GetProfileUseCase(userNetworkRepository, userLocalRepository, tokenRepository)
    }

    @Test
    fun `invoke with cached user emits cached value first`() = runTest {
        // Given
        val cachedUser = mockk<User>(relaxed = true)
        val accessToken = "access_token"

        coEvery { userLocalRepository.getCachedUser() } returns cachedUser
        every { tokenRepository.getAccessToken() } returns accessToken

        // When
        val result = getProfileUseCase().first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedUser, result.getOrNull())
    }

    @Test
    fun `invoke without cached user but with token fetches from network`() = runTest {
        // Given
        val networkUser = mockk<User>(relaxed = true)
        val accessToken = "access_token"

        coEvery { userLocalRepository.getCachedUser() } returns null
        every { tokenRepository.getAccessToken() } returns accessToken
        coEvery { userNetworkRepository.getProfile(accessToken) } returns Result.success(networkUser)
        coEvery { userLocalRepository.upsertUser(networkUser) } returns Unit

        // When
        val result = getProfileUseCase().first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(networkUser, result.getOrNull())
        coVerify { userLocalRepository.upsertUser(networkUser) }
    }

    @Test
    fun `invoke without token and without cache emits error`() = runTest {
        // Given
        coEvery { userLocalRepository.getCachedUser() } returns null
        every { tokenRepository.getAccessToken() } returns ""

        // When
        val result = getProfileUseCase().first()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `invoke when network fails but has cache emits cached value`() = runTest {
        // Given
        val cachedUser = mockk<User>(relaxed = true)
        val accessToken = "access_token"
        val error = Exception("Network error")

        coEvery { userLocalRepository.getCachedUser() } returns cachedUser
        every { tokenRepository.getAccessToken() } returns accessToken
        coEvery { userNetworkRepository.getProfile(accessToken) } returns Result.failure(error)

        // When
        val result = getProfileUseCase().first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedUser, result.getOrNull())
    }

    @Test
    fun `invoke when network fails without cache emits error`() = runTest {
        // Given
        val accessToken = "access_token"
        val error = Exception("Network error")

        coEvery { userLocalRepository.getCachedUser() } returns null
        every { tokenRepository.getAccessToken() } returns accessToken
        coEvery { userNetworkRepository.getProfile(accessToken) } returns Result.failure(error)

        // When
        val result = getProfileUseCase().first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
