package com.fruits.network.recommendations.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsSchema (
    val id: String,
    @SerialName("author_id")    val authorId: String,
    @SerialName("author_name")  val authorName: String,
    @SerialName("author_avatar") val authorAvatarUrl: String,
    @SerialName("image_url")    val imageUrl: String?,
    val description: String,
    @SerialName("likes_count")  val likesCount: Int,
    @SerialName("is_liked")     val isLiked: Boolean,
    val explanation: List<String>
)