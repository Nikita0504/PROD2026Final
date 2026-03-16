package com.fruits.domain.usecase.interactions

import com.fruits.domain.model.interactions.UserAction
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

class SendUserActionUseCaseTest {

    @MockK
    private lateinit var interactionsRepository: InteractionsRepository
    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var useCase: SendUserActionUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SendUserActionUseCase(interactionsRepository, tokenRepository)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        val targetUserId = "user123"
        val action = UserAction.LIKE
        val accessToken = "token123"

        coEvery { tokenRepository.getAccessToken() } returns accessToken
        coEvery { 
            interactionsRepository.sendAction(accessToken, targetUserId, action) 
        } returns Result.success(Unit)

        val result = useCase(targetUserId, action)

        assertTrue(result.isSuccess)
        coVerify { interactionsRepository.sendAction(accessToken, targetUserId, action) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = Exception("Network error")
        coEvery { tokenRepository.getAccessToken() } returns "token"
        coEvery { 
            interactionsRepository.sendAction(any(), any(), any()) 
        } returns Result.failure(error)

        val result = useCase("id", UserAction.DISLIKE)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
