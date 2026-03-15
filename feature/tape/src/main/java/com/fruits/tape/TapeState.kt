package com.fruits.tape

data class TapeCardItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val reasonInFeed: String,
    val about: String,
)

data class TapeState(
    val currentCard: TapeCardItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)