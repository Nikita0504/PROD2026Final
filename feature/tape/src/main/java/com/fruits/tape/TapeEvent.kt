package com.fruits.tape

import com.fruits.tape.components.swipe_card.SwipeDirection

sealed interface TapeEvent {
    data class OnTabSelected(val tab: TapeTab) : TapeEvent
    data object OnWhyClicked : TapeEvent
    data object OnAboutClicked : TapeEvent
    data class OnCardSwiped(val direction: SwipeDirection) : TapeEvent
    data object OnRetry : TapeEvent
    data class OnLikedCardSwiped(val direction: SwipeDirection) : TapeEvent
    data object OnLikedRetry : TapeEvent
    data object OnLikedAboutClicked : TapeEvent
}
