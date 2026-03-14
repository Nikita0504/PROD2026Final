package com.fruits.repository.recommendations.mapper

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.network.recommendations.schema.RecommendationsSchema

object RecommendationsMapper {
    fun RecommendationsSchema.toDomain() : Recommendations = Recommendations(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        imageUrl = imageUrl,
        description = description,
        likesCount = likesCount,
        isLiked = isLiked
    )

    fun List<RecommendationsSchema>.toDomain(): List<Recommendations> = map { it.toDomain() }
}