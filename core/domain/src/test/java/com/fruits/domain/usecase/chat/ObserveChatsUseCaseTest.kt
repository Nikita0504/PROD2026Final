package com.fruits.domain.usecase.chat

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.repository.ChatRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ObserveChatsUseCaseTest {

    @MockK
    private lateinit var chatRepository: ChatRepository

    private lateinit var useCase: ObserveChatsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ObserveChatsUseCase(chatRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val chats = listOf(mockk<Chat>())
        every { chatRepository.observeChats() } returns flowOf(chats)

        val result = useCase()

        result.collect {
            assertEquals(chats, it)
        }
    }
}
