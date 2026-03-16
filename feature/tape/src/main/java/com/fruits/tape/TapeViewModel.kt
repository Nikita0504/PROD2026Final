package com.fruits.tape

import com.fruits.logger.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.usecase.interactions.GetIncomingLikesUseCase
import com.fruits.domain.usecase.interactions.SendUserActionUseCase
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import com.fruits.tape.components.swipe_card.SwipeDirection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TapeViewModel(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getIncomingLikesUseCase: GetIncomingLikesUseCase,
    private val sendUserActionUseCase: SendUserActionUseCase,
    private val imageUploadUrlRepository: ImageUploadUrlRepository,
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
            is TapeEvent.OnTabSelected -> {
                _state.update { it.copy(selectedTab = event.tab) }
                if (event.tab == TapeTab.Liked && _state.value.likedCards.isEmpty() && !_state.value.likedIsLoading) {
                    loadLiked()
                }
            }
            TapeEvent.OnWhyClicked -> _state.value.currentCard?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowReasonSheet(it.reasonInFeed)) }
            }
            TapeEvent.OnAboutClicked -> _state.value.currentCard?.let {
                viewModelScope.launch { _effects.emit(TapeEffect.ShowAboutSheet(it.about)) }
            }
            is TapeEvent.OnCardSwiped -> handleSwipe(event.direction)
            TapeEvent.OnRetry -> loadRecommendations()
        }
    }

    private fun handleSwipe(direction: SwipeDirection) {
        val current = _state.value.currentCard ?: run {
            nextCard()
            return
        }

        val action = when (direction) {
            SwipeDirection.LEFT -> UserAction.DISLIKE
            SwipeDirection.RIGHT -> UserAction.LIKE
            else -> null
        }

        if (action != null) {
            viewModelScope.launch {
                Log.d(TAG, "Sending user action: userId=${current.userId}, action=$action, url:${current.imageUrl} ")
                val result = sendUserActionUseCase(
                    targetUserId = current.userId,
                    action = action,
                )
                result
                    .onSuccess {
                        Log.i(TAG, "User action sent successfully: userId=${current.userId}, action=$action")
                    }
                    .onFailure { error ->
                        Log.w(TAG, "Failed to send user action: userId=${current.userId}, action=$action, error=${error.message}")
                    }
            }
        }

        nextCard()
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
                    val photoKeys = list.mapNotNull { it.photoFileKeys.firstOrNull() }.distinct()
                    val urlMap = if (photoKeys.isEmpty()) emptyMap()
                    else imageUploadUrlRepository.getDownloadUrls(photoKeys)
                        .getOrNull()
                        ?.associate { it.key to it.url }
                        ?: emptyMap()
                    Log.d(TAG, "Resolved ${urlMap.size} image URLs for ${photoKeys.size} keys")
                    buffer.addAll(list.map { it.toCardItem(urlMap) })
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

    private fun loadLiked() {
        viewModelScope.launch {
            _state.update { it.copy(likedIsLoading = true, likedError = null) }
            getIncomingLikesUseCase()
                .onSuccess { list ->
                    Log.d(TAG, "=== Loaded ${list.size} incoming likes ===")
                    list.forEachIndexed { i, r ->
                        Log.d(TAG, "[$i] ${r.firstName} ${r.secondName}, age=${r.age}, city=${r.city}")
                        Log.d(TAG, "[$i] imageKeys=${r.photoFileKeys}")
                    }
                    val photoKeys = list.mapNotNull { it.photoFileKeys.firstOrNull() }.distinct()
                    val urlMap = if (photoKeys.isEmpty()) emptyMap()
                    else imageUploadUrlRepository.getDownloadUrls(photoKeys)
                        .getOrNull()
                        ?.associate { it.key to it.url }
                        ?: emptyMap()

                    val cards = list.map { it.toLikedCardItem(urlMap) }
                    _state.update {
                        it.copy(
                            likedIsLoading = false,
                            likedCards = cards,
                            likedError = null,
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load incoming likes: ${error.message}")
                    _state.update {
                        it.copy(
                            likedIsLoading = false,
                            likedError = error.message,
                        )
                    }
                }
        }
    }

    companion object {
        private const val TAG = "TapeViewModel"
    }
}

private fun Recommendations.toCardItem(urlMap: Map<String, String>) = TapeCardItem(
    userId = userId,
    name = "$firstName $secondName",
    age = age,
    city = city,
    imageUrl = photoFileKeys.firstOrNull()?.let { urlMap[it] } ?: "",
    reasonInFeed = explanation,
    about = description
)

private fun IncomingLike.toLikedCardItem(urlMap: Map<String, String>) = TapeCardItem(
    userId = likedByUserId,
    name = "$firstName $secondName",
    age = age,
    city = city,
    imageUrl = photoFileKeys.firstOrNull()?.let { urlMap[it] } ?: "",
    reasonInFeed = emptyList(),
    about = description,
)
