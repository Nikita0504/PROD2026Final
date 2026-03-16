package com.fruits.domain.usecase.chat

import com.fruits.domain.model.chat.SentMessage
import com.fruits.domain.repository.ChatRepository
import com.fruits.domain.repository.TokenRepository
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

class SendChatMessageUseCaseTest {

    @MockK
    private lateinit var chatRepository: ChatRepository
    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var useCase: SendChatMessageUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SendChatMessageUseCase(chatRepository, tokenRepository)
    }

    @Test
    fun `invoke with no access token returns failure`() = runTest {
        coEvery { tokenRepository.getAccessToken() } returns ""

        val result = useCase("chatId", "text")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("Не авторизован", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid token calls repository`() = runTest {
        val sentMessage = mockk<SentMessage>()
        coEvery { tokenRepository.getAccessToken() } returns "valid_token"
        coEvery { chatRepository.sendMessage("valid_token", "chatId", "text") } returns Result.success(sentMessage)

        val result = useCase("chatId", "text")

        assertTrue(result.isSuccess)
        assertEquals(sentMessage, result.getOrNull())
        coVerify { chatRepository.sendMessage("valid_token", "chatId", "text") }
    }

    @Test
    fun `invoke with repository failure returns failure`() = runTest {
        val error = Exception("Network error")
        coEvery { tokenRepository.getAccessToken() } returns "valid_token"
        coEvery { chatRepository.sendMessage(any(), any(), any()) } returns Result.failure(error)

        val result = useCase("chatId", "text")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
