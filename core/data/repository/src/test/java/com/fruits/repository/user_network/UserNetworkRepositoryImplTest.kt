package com.fruits.repository.user_network

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.network.user.schema.UserLoginSchema
import com.fruits.network.user.schema.UserReadSchema
import com.fruits.network.user.service.UserService
import com.fruits.network.util.ApiResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserNetworkRepositoryImplTest {

    @MockK
    private lateinit var service: UserService

    @MockK
    private lateinit var mockStorage: MockStorage

    @MockK
    private lateinit var mockDataService: MockDataService

    private lateinit var repository: UserNetworkRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = UserNetworkRepositoryImpl(service, mockStorage, mockDataService)
    }

    @Test
    fun `login with mock disabled calls network service and returns tokens`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkTokens = mockk<com.fruits.network.user.schema.TokenReadSchema>(relaxed = true)
        every { networkTokens.accessToken } returns "access_token"
        every { networkTokens.refreshToken } returns "refresh_token"
        every { mockStorage.enabled } returns false

        coEvery { service.login(UserLoginSchema(email, password)) } returns ApiResult.Success(networkTokens)

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("access_token", result.getOrNull()?.accessToken)
        assertEquals("refresh_token", result.getOrNull()?.refreshToken)
    }

    @Test
    fun `login with mock enabled returns mock tokens`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        every { mockStorage.enabled } returns true
        every { mockDataService.tokensMock.accessToken } returns "mock_access"
        every { mockDataService.tokensMock.refreshToken } returns "mock_refresh"

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("mock_access", result.getOrNull()?.accessToken)
    }

    @Test
    fun `login when service returns error returns failure`() = runTest {
        // Given
        val email = "wrong@example.com"
        val password = "wrong"
        every { mockStorage.enabled } returns false

        coEvery { service.login(any()) } returns ApiResult.Error( "Invalid credentials", 403)

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getProfile with mock disabled calls network and returns user`() = runTest {
        // Given
        val accessToken = "access_token"
        val networkUser = mockk<UserReadSchema>(relaxed = true)
        every { networkUser.id } returns "user_123"
        every { networkUser.email } returns "test@example.com"
        every { networkUser.firstName } returns "John"
        every { networkUser.secondName } returns "Doe"
        every { networkUser.readyToGive } returns true
        every { networkUser.avatarFileKey } returns null
        every { networkUser.description } returns null
        every { networkUser.photoFileKeys } returns emptyList()
        every { mockStorage.enabled } returns false

        coEvery { service.getProfile(accessToken) } returns ApiResult.Success(networkUser)

        // When
        val result = repository.getProfile(accessToken)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals("user_123", user?.id)
        assertEquals("test@example.com", user?.email)
    }

    @Test
    fun `getProfile with mock enabled returns mock user`() = runTest {
        // Given
        val accessToken = "any_token"
        every { mockStorage.enabled } returns true
        every { mockDataService.userMock.id } returns "mock_user_id"
        every { mockDataService.userMock.email } returns "mock@test.com"

        // When
        val result = repository.getProfile(accessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("mock_user_id", result.getOrNull()?.id)
    }

    @Test
    fun `getProfile when service returns error returns failure`() = runTest {
        // Given
        val accessToken = "invalid_token"
        every { mockStorage.enabled } returns false

        coEvery { service.getProfile(accessToken) } returns ApiResult.Error( "Unauthorized", 401)

        // When
        val result = repository.getProfile(accessToken)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshToken with mock disabled calls network and returns tokens`() = runTest {
        // Given
        val refreshToken = "refresh_123"
        val networkTokens = mockk<com.fruits.network.user.schema.TokenReadSchema>(relaxed = true)
        every { networkTokens.accessToken } returns "new_access"
        every { networkTokens.refreshToken } returns "new_refresh"
        every { mockStorage.enabled } returns false

        coEvery { service.refreshToken(refreshToken) } returns ApiResult.Success(networkTokens)

        // When
        val result = repository.refreshToken(refreshToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("new_access", result.getOrNull()?.accessToken)
    }

    @Test
    fun `refreshToken when service returns error returns failure`() = runTest {
        // Given
        val refreshToken = "expired_refresh"
        every { mockStorage.enabled } returns false

        coEvery { service.refreshToken(any()) } returns ApiResult.Error( "Invalid refresh token", 401)

        // When
        val result = repository.refreshToken(refreshToken)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `patchProfile with valid data returns updated user`() = runTest {
        // Given
        val accessToken = "access_token"
        val updateData = com.fruits.domain.model.user.UserProfileUpdate(
            description = "New description",
            photoFilesKeys = listOf("key1", "key2")
        )
        val networkUser = mockk<UserReadSchema>(relaxed = true)
        every { networkUser.id } returns "user_123"
        every { networkUser.description } returns "New description"
        every { mockStorage.enabled } returns false

        coEvery { service.patchProfile(any(), accessToken) } returns ApiResult.Success(networkUser)

        // When
        val result = repository.patchProfile(updateData, accessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("New description", result.getOrNull()?.description)
    }

    @Test
    fun `patchProfile when service returns error returns failure`() = runTest {
        // Given
        val accessToken = "access_token"
        val updateData = com.fruits.domain.model.user.UserProfileUpdate(
            description = "New desc",
            photoFilesKeys = emptyList()
        )
        every { mockStorage.enabled } returns false

        coEvery { service.patchProfile(any(), any()) } returns ApiResult.Error( "Unauthorized", 401)

        // When
        val result = repository.patchProfile(updateData, accessToken)

        // Then
        assertTrue(result.isFailure)
    }
}
