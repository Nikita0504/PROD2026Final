package com.fruits.domain.usecase.chat

import com.fruits.domain.model.chat.ChatDetail
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

class GetChatUseCaseTest {

    @MockK
    private lateinit var chatRepository: ChatRepository
    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var useCase: GetChatUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetChatUseCase(chatRepository, tokenRepository)
    }

    @Test
    fun `invoke with no access token returns failure`() = runTest {
        coEvery { tokenRepository.getAccessToken() } returns ""

        val result = useCase("chatId")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("Не авторизован", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid token calls repository`() = runTest {
        val chatDetail = mockk<ChatDetail>()
        coEvery { tokenRepository.getAccessToken() } returns "valid_token"
        coEvery { chatRepository.getChat("valid_token", "chatId") } returns Result.success(chatDetail)

        val result = useCase("chatId")

        assertTrue(result.isSuccess)
        assertEquals(chatDetail, result.getOrNull())
        coVerify { chatRepository.getChat("valid_token", "chatId") }
    }

    @Test
    fun `invoke with repository failure returns failure`() = runTest {
        val error = Exception("Chat not found")
        coEvery { tokenRepository.getAccessToken() } returns "valid_token"
        coEvery { chatRepository.getChat(any(), any()) } returns Result.failure(error)

        val result = useCase("chatId")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
