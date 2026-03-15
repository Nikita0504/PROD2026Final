package com.fruits.domain.model.recommendations

data class Recommendations (
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val imageUrl: String?,
    val description: String,
    val likesCount: Int,
    val isLiked: Boolean
)