package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.domain.repository.TokenRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportUserUseCaseTest {

    @MockK
    private lateinit var interactionsRepository: InteractionsRepository
    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var useCase: ReportUserUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ReportUserUseCase(interactionsRepository, tokenRepository)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        val targetUserId = "user123"
        val reason = ReportReason.SPAM
        val comment = "Spamming messages"
        val accessToken = "token123"

        coEvery { tokenRepository.getAccessToken() } returns accessToken
        coEvery { 
            interactionsRepository.reportUser(accessToken, targetUserId, reason, comment) 
        } returns Result.success(Unit)

        val result = useCase(targetUserId, reason, comment)

        assertTrue(result.isSuccess)
        coVerify { interactionsRepository.reportUser(accessToken, targetUserId, reason, comment) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = Exception("Network error")
        coEvery { tokenRepository.getAccessToken() } returns "token"
        coEvery { 
            interactionsRepository.reportUser(any(), any(), any(), any()) 
        } returns Result.failure(error)

        val result = useCase("id", ReportReason.OTHER, null)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
