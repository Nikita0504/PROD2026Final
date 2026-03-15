package com.fruits.tape

data class TapeCardItem(
    val userId: String,
    val name: String,
    val age: Int,
    val city: String,
    val imageUrl: String,
    val reasonInFeed: List<String>,
    val about: String,
)

data class TapeState(
    val currentCard: TapeCardItem? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
)
