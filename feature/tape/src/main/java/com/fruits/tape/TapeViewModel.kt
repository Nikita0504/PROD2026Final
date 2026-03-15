package com.fruits.tape

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
            TapeEvent.OnWhyClicked -> _state.value.currentCard?.reasonInFeed?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowReasonSheet(it)) }
            }
            TapeEvent.OnAboutClicked -> _state.value.currentCard?.about?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowAboutSheet(it)) }
            }
            TapeEvent.OnCardSwiped -> nextCard()
            TapeEvent.OnRetry -> loadRecommendations()
        }
    }

    private fun nextCard() {
        if (buffer.isEmpty()) {
            loadRecommendations()
            return
        }
        _state.update { it.copy(currentCard = buffer.removeFirst(), error = null) }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getRecommendationsUseCase()
                .onSuccess { list ->
                    buffer.addAll(list.map { it.toCardItem() })
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentCard = buffer.removeFirstOrNull()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }
}

private fun Recommendations.toCardItem() = TapeCardItem(
    id = id,
    name = authorName,
    imageUrl = imageUrl ?: "",
    reasonInFeed = description,
    about = description
)
