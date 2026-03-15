package com.fruits.tape

sealed interface TapeEvent {
    data object OnWhyClicked : TapeEvent
    data object OnAboutClicked : TapeEvent
    data object OnCardSwiped : TapeEvent
    data object OnRetry : TapeEvent
}
