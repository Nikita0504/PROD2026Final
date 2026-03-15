package com.fruits.tape

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.collections.emptyList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TapeViewModelTest {

    @MockK
    private lateinit var getRecommendationsUseCase: GetRecommendationsUseCase

    private lateinit var viewModel: TapeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TapeViewModel(getRecommendationsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
