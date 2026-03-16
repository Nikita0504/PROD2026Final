package com.fruits.domain.usecase.chat

import com.fruits.domain.repository.ChatRepository
import com.fruits.domain.repository.TokenRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RefreshChatsUseCaseTest {

    @MockK
    private lateinit var chatRepository: ChatRepository
    @MockK
    private lateinit var tokenRepository: TokenRepository

    private lateinit var useCase: RefreshChatsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = RefreshChatsUseCase(chatRepository, tokenRepository)
    }

    @Test
    fun `invoke with no access token throws IllegalStateException`() = runTest {
        coEvery { tokenRepository.getAccessToken() } returns ""

        val exception = assertFailsWith<IllegalStateException> {
            useCase()
        }
        assertEquals("Не авторизован", exception.message)
    }

    @Test
    fun `invoke with valid token calls refreshChats`() = runTest {
        coEvery { tokenRepository.getAccessToken() } returns "valid_token"
        coEvery { chatRepository.refreshChats("valid_token") } returns Unit

        useCase()

        coVerify { chatRepository.refreshChats("valid_token") }
    }
}
