package com.fruits.session

import com.fruits.domain.model.SessionState
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.logger.Log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @MockK
    private lateinit var loginUseCase: LoginUseCase
    @MockK
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    @MockK
    private lateinit var fcmTokenProvider: FcmTokenProvider
    @MockK
    private lateinit var tokenRepository: TokenRepository
    @MockK
    private lateinit var userLocalRepository: UserLocalRepository
    @MockK
    private lateinit var userNetworkRepository: UserNetworkRepository

    private lateinit var sessionManager: SessionManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        mockkObject(Log)
        every { Log.v(any(), any()) } returns Unit
        every { Log.d(any(), any()) } returns Unit
        every { Log.i(any(), any()) } returns Unit
        every { Log.w(any(), any()) } returns Unit
        every { Log.e(any(), any()) } returns Unit
        every { Log.e(any(), any(), any()) } returns Unit

        sessionManager = SessionManager(
            loginUseCase,
            updateProfileUseCase,
            fcmTokenProvider,
            tokenRepository,
            userLocalRepository,
            userNetworkRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        assertEquals(SessionState.Loading, sessionManager.sessionState.value)
    }

    @Test
    fun `restoreSession with no refresh token sets Unauthorized`() = runTest {
        coEvery { tokenRepository.getRefreshToken() } returns ""

        sessionManager.restoreSession()
        advanceUntilIdle()

        assertTrue(sessionManager.sessionState.value is SessionState.Unauthorized)
    }

    @Test
    fun `restoreSession with valid refresh token and ready user sets Authorized`() = runTest {
        val tokens = Tokens("access", "refresh")
        val user = mockk<User>()
        every { user.readyToGive } returns true
        
        coEvery { tokenRepository.getRefreshToken() } returns "refresh"
        coEvery { userNetworkRepository.refreshToken("refresh") } returns Result.success(tokens)
        coEvery { tokenRepository.saveTokens(any(), any()) } returns Unit
        coEvery { userLocalRepository.getCachedUser() } returns user

        sessionManager.restoreSession()
        advanceUntilIdle()

        assertEquals(SessionState.Authorized, sessionManager.sessionState.value)
    }

    @Test
    fun `restoreSession with valid refresh token and not ready user sets Onboarding`() = runTest {
        val tokens = Tokens("access", "refresh")
        val user = mockk<User>()
        every { user.readyToGive } returns false

        coEvery { tokenRepository.getRefreshToken() } returns "refresh"
        coEvery { userNetworkRepository.refreshToken("refresh") } returns Result.success(tokens)
        coEvery { tokenRepository.saveTokens(any(), any()) } returns Unit
        coEvery { userLocalRepository.getCachedUser() } returns user

        sessionManager.restoreSession()
        advanceUntilIdle()

        assertTrue(sessionManager.sessionState.value is SessionState.Onboarding)
    }

    @Test
    fun `login success with ready user sets Authorized`() = runTest {
        val user = mockk<User>()
        every { user.readyToGive } returns true
        coEvery { fcmTokenProvider.getFcmToken() } returns "fcm"
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(user)

        sessionManager.login("test@test.com", "password")
        advanceUntilIdle()

        assertEquals(SessionState.Authorized, sessionManager.sessionState.value)
    }

    @Test
    fun `login failure sets Unauthorized with error`() = runTest {
        coEvery { fcmTokenProvider.getFcmToken() } returns "fcm"
        coEvery { loginUseCase(any(), any(), any()) } returns Result.failure(Exception("Login error"))

        sessionManager.login("test@test.com", "password")
        advanceUntilIdle()

        val state = sessionManager.sessionState.value
        assertTrue(state is SessionState.Unauthorized)
        assertEquals("Login error", (state as SessionState.Unauthorized).error)
    }

    @Test
    fun `logout clears session and sets Unauthorized`() = runTest {
        coEvery { tokenRepository.clear() } returns Unit
        coEvery { userLocalRepository.clearCache() } returns Unit

        sessionManager.logout()
        advanceUntilIdle()

        assertTrue(sessionManager.sessionState.value is SessionState.Unauthorized)
        coVerify { tokenRepository.clear() }
        coVerify { userLocalRepository.clearCache() }
    }

    @Test
    fun `toggleDebug switches between Debug and previous state`() = runTest {
        // Initial state is Loading
        sessionManager.toggleDebug()
        assertTrue(sessionManager.sessionState.value is SessionState.Debug)

        sessionManager.toggleDebug()
        assertEquals(SessionState.Loading, sessionManager.sessionState.value)
    }
}
