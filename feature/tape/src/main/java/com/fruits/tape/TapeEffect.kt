package com.fruits.tape

sealed interface TapeEffect {
    data class ShowReasonSheet(val reasons: List<String>) : TapeEffect
    data class ShowAboutSheet(val about: String) : TapeEffect
}
