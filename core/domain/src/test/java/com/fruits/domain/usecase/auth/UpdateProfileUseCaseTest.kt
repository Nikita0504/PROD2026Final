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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateProfileUseCaseTest {

    @MockK
    private lateinit var userNetworkRepository: UserNetworkRepository

    @MockK
    private lateinit var tokensRepository: TokenRepository

    @MockK
    private lateinit var userLocalRepository: UserLocalRepository

    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        updateProfileUseCase = UpdateProfileUseCase(userNetworkRepository, tokensRepository, userLocalRepository)
    }

    @Test
    fun `invoke with valid data returns updated user`() = runTest {
        // Given
        val description = "Updated description"
        val photoKeys = listOf("key1", "key2")
        val accessToken = "access_token"
        val updatedUser = mockk<User>(relaxed = true)

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { userNetworkRepository.patchProfile(any(), accessToken) } returns Result.success(updatedUser)
        coEvery { userLocalRepository.upsertUser(updatedUser) } returns Unit

        // When
        val result = updateProfileUseCase(description, photoKeys)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(updatedUser, result.getOrNull())
        coVerify {
            userNetworkRepository.patchProfile(
                match { it.description == description && it.photoFilesKeys == photoKeys },
                accessToken
            )
        }
        coVerify { userLocalRepository.upsertUser(updatedUser) }
    }

    @Test
    fun `invoke when network fails returns error`() = runTest {
        // Given
        val description = "Updated description"
        val photoKeys = listOf("key1")
        val accessToken = "access_token"
        val error = Exception("Network error")

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { userNetworkRepository.patchProfile(any(), any()) } returns Result.failure(error)

        // When
        val result = updateProfileUseCase(description, photoKeys)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
