//package com.fruits.chat
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class ChatViewModelTest {
//
//    private lateinit var viewModel: ChatViewModel
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        viewModel = ChatViewModel()
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `initial state has empty messages list`() = runTest {
//        // Then
//        val state = viewModel.state.value
//        assertTrue(state.messages.isEmpty())
//        assertEquals("", state.inputText)
//    }
//
//    @Test
//    fun `initial state loads mock messages`() = runTest {
//        // When
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(3, state.messages.size)
//        assertEquals("Привет! Как дела?", state.messages[0].text)
//        assertEquals("Нормально, работаю над проектом.", state.messages[1].text)
//        assertEquals("Круто. Когда покажешь?", state.messages[2].text)
//    }
//
//    @Test
//    fun `onEvent InputChanged updates input text`() = runTest {
//        // When
//        viewModel.onEvent(ChatEvent.InputChanged("Hello"))
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals("Hello", state.inputText)
//    }
//
//    @Test
//    fun `onEvent SendClicked with empty text does nothing`() = runTest {
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(3, state.messages.size)
//        assertEquals("", state.inputText)
//    }
//
//    @Test
//    fun `onEvent SendClicked with whitespace text does nothing`() = runTest {
//        // Given
//        viewModel.onEvent(ChatEvent.InputChanged("   "))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(3, state.messages.size)
//    }
//
//    @Test
//    fun `onEvent SendClicked adds message to list`() = runTest {
//        // Given
//        viewModel.onEvent(ChatEvent.InputChanged("New message"))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(4, state.messages.size)
//        assertEquals("New message", state.messages[3].text)
//        assertTrue(state.messages[3].isOwn)
//    }
//
//    @Test
//    fun `onEvent SendClicked clears input text`() = runTest {
//        // Given
//        viewModel.onEvent(ChatEvent.InputChanged("Test message"))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals("", state.inputText)
//    }
//
//    @Test
//    fun `sent message has generated id`() = runTest {
//        // Given
//        viewModel.onEvent(ChatEvent.InputChanged("Test"))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        val newMessage = state.messages.find { it.text == "Test" }
//        assertEquals(true, newMessage?.id?.isNotEmpty())
//    }
//
//    @Test
//    fun `sent message has current timestamp`() = runTest {
//        // Given
//        val beforeTime = System.currentTimeMillis()
//        viewModel.onEvent(ChatEvent.InputChanged("Test"))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        val newMessage = state.messages.find { it.text == "Test" }
//        assertTrue(newMessage?.timestampMillis ?: 0 >= beforeTime)
//    }
//
//    @Test
//    fun `multiple messages can be sent sequentially`() = runTest {
//        // When
//        viewModel.onEvent(ChatEvent.InputChanged("Message 1"))
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        viewModel.onEvent(ChatEvent.InputChanged("Message 2"))
//        viewModel.onEvent(ChatEvent.SendClicked)
//        advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(5, state.messages.size)
//        assertEquals("Message 1", state.messages[3].text)
//        assertEquals("Message 2", state.messages[4].text)
//    }
//}
