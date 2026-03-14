package com.fruits.tape

sealed interface TapeEffect {
    data class ShowReasonSheet(val reason: String) : TapeEffect
    data class ShowAboutSheet(val about: String) : TapeEffect
}