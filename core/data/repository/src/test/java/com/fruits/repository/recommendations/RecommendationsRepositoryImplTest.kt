package com.fruits.repository.recommendations

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.logger.Log
import com.fruits.network.recommendations.schema.RecommendationsResponse
import com.fruits.network.recommendations.schema.RecommendationsSchema
import com.fruits.network.recommendations.service.RecommendationsService
import com.fruits.network.util.ApiResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationsRepositoryImplTest {

    @MockK
    private lateinit var service: RecommendationsService

    @MockK
    private lateinit var mockStorage: MockStorage

    @MockK
    private lateinit var mockDataService: MockDataService

    private lateinit var repository: RecommendationsRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Мокаем Log чтобы избежать вызова android.util.Log
        mockkObject(Log)
        every { Log.d(any(), any()) } returns Unit
        every { Log.w(any(), any()) } returns Unit
        every { Log.i(any(), any()) } returns Unit
        every { Log.e(any(), any()) } returns Unit
        repository = RecommendationsRepositoryImpl(service, mockStorage, mockDataService)
    }

    @After
    fun tearDown() {
        unmockkObject(Log)
    }

    @Test
    fun `getRecommendations with mock disabled calls service and returns recommendations`() = runTest {
        // Given
        val accessToken = "access_token"
        val schemas = listOf(
            RecommendationsSchema(
                userId = "user_1",
                firstName = "John",
                secondName = "Doe",
                age = 25,
                city = "Moscow",
                photoFileKeys = listOf("photo1"),
                description = "Description 1",
                explanation = listOf("Explanation 1")
            ),
            RecommendationsSchema(
                userId = "user_2",
                firstName = "Jane",
                secondName = "Smith",
                age = 30,
                city = "SPb",
                photoFileKeys = listOf("photo2", "photo3"),
                description = "Description 2",
                explanation = listOf("Explanation 2")
            )
        )
        val response = RecommendationsResponse(candidates = schemas, alUsed = false)

        every { mockStorage.enabled } returns false
        coEvery { service.getRecommendations(accessToken) } returns ApiResult.Success(response)

        // When
        val result = repository.getRecommendations(accessToken)

        // Then
        assertTrue(result.isSuccess)
        val recommendations = result.getOrNull()
        assertEquals(2, recommendations?.size)
        assertEquals("user_1", recommendations?.get(0)?.userId)
        assertEquals("user_2", recommendations?.get(1)?.userId)
    }

    @Test
    fun `getRecommendations with mock enabled returns mock data`() = runTest {
        // Given
        val accessToken = "any_token"
        val mockRecommendations = listOf(
            com.fruits.domain.model.recommendations.Recommendations(
                userId = "mock_user",
                firstName = "Mock",
                secondName = "User",
                age = 20,
                city = "Mock City",
                photoFileKeys = listOf("mock_photo"),
                description = "Mock description",
                explanation = listOf("Mock explanation")
            )
        )

        every { mockStorage.enabled } returns true
        every { mockDataService.recommendations } returns mockRecommendations

        // When
        val result = repository.getRecommendations(accessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("mock_user", result.getOrNull()?.get(0)?.userId)
    }

    @Test
    fun `getRecommendations when service returns error returns failure`() = runTest {
        // Given
        val accessToken = "invalid_token"

        every { mockStorage.enabled } returns false
        coEvery { service.getRecommendations(accessToken) } returns ApiResult.Error("Unauthorized", 401)

        // When
        val result = repository.getRecommendations(accessToken)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getRecommendations with empty candidates returns empty list`() = runTest {
        // Given
        val accessToken = "access_token"
        val response = RecommendationsResponse(candidates = emptyList(), alUsed = false)

        every { mockStorage.enabled } returns false
        coEvery { service.getRecommendations(accessToken) } returns ApiResult.Success(response)

        // When
        val result = repository.getRecommendations(accessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }
}
