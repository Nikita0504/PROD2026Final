package com.fruits.domain.usecase.recommendations

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.repository.RecommendationsRepository
import com.fruits.domain.repository.TokenRepository
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

class GetRecommendationsUseCaseTest {

    @MockK
    private lateinit var recommendationsRepository: RecommendationsRepository

    @MockK
    private lateinit var tokensRepository: TokenRepository

    private lateinit var getRecommendationsUseCase: GetRecommendationsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getRecommendationsUseCase = GetRecommendationsUseCase(recommendationsRepository, tokensRepository)
    }

    @Test
    fun `invoke returns recommendations from repository`() = runTest {
        // Given
        val accessToken = "access_token"
        val recommendations = listOf(
            mockk<Recommendations>(relaxed = true),
            mockk<Recommendations>(relaxed = true)
        )

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { recommendationsRepository.getRecommendations(accessToken) } returns Result.success(recommendations)

        // When
        val result = getRecommendationsUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(recommendations, result.getOrNull())
    }

    @Test
    fun `invoke when repository fails returns error`() = runTest {
        // Given
        val accessToken = "access_token"
        val error = Exception("Network error")

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { recommendationsRepository.getRecommendations(accessToken) } returns Result.failure(error)

        // When
        val result = getRecommendationsUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke with empty token still calls repository`() = runTest {
        // Given
        val accessToken = ""
        val error = Exception("Unauthorized")

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { recommendationsRepository.getRecommendations(accessToken) } returns Result.failure(error)

        // When
        val result = getRecommendationsUseCase()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        // Given
        val accessToken = "access_token"

        every { tokensRepository.getAccessToken() } returns accessToken
        coEvery { recommendationsRepository.getRecommendations(accessToken) } returns Result.success(emptyList())

        // When
        val result = getRecommendationsUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }
}
