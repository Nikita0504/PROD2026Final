package com.fruits.tape

import com.fruits.debug.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TapeViewModel(
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TapeState())
    val state: StateFlow<TapeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TapeEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TapeEffect> = _effects.asSharedFlow()

    private val buffer = ArrayDeque<TapeCardItem>()

    init {
        loadRecommendations()
    }

    fun onEvent(event: TapeEvent) {
        when (event) {
            TapeEvent.OnWhyClicked -> _state.value.currentCard?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowReasonSheet(it.reasonInFeed)) }
            }
            TapeEvent.OnAboutClicked -> _state.value.currentCard?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowAboutSheet(it.about)) }
            }
            is TapeEvent.OnCardSwiped -> nextCard()
            TapeEvent.OnRetry -> loadRecommendations()
        }
    }

    private fun nextCard() {
        if (buffer.isEmpty()) {
            loadRecommendations()
            return
        }
        _state.update { it.copy(currentCard = buffer.removeFirst(), error = null, isEmpty = false) }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isEmpty = false) }
            getRecommendationsUseCase()
                .onSuccess { list ->
                    Log.d(TAG, "=== Loaded ${list.size} recommendations ===")
                    list.forEachIndexed { i, r ->
                        Log.d(TAG, "[$i] ${r.firstName} ${r.secondName}, age=${r.age}, city=${r.city}")
                        Log.d(TAG, "[$i] imageUrl=${r.photoFileKeys.firstOrNull()}")
                        Log.d(TAG, "[$i] explanation(${r.explanation.size})=${r.explanation}")
                    }
                    buffer.addAll(list.map { it.toCardItem() })
                    val first = buffer.removeFirstOrNull()
                    Log.d(TAG, "First card: userId=${first?.userId}, imageUrl=${first?.imageUrl}, reasons=${first?.reasonInFeed}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentCard = first,
                            isEmpty = first == null,
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load recommendations: ${error.message}")
                    _state.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    companion object {
        private const val TAG = "TapeViewModel"
    }
}

private fun Recommendations.toCardItem() = TapeCardItem(
    userId = userId,
    name = "$firstName $secondName",
    age = age,
    city = city,
    imageUrl = photoFileKeys.firstOrNull() ?: "",
    reasonInFeed = explanation,
    about = description
)
