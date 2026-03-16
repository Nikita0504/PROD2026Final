package com.fruits.tape

enum class TapeTab {
    Recommendations,
    Liked,
}

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
    val selectedTab: TapeTab = TapeTab.Recommendations,
    val currentCard: TapeCardItem? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val likedCurrentCard: TapeCardItem? = null,
    val likedIsLoading: Boolean = false,
    val likedIsEmpty: Boolean = false,
    val likedError: String? = null,
)
