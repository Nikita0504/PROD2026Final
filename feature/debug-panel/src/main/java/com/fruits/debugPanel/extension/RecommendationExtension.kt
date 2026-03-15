package com.fruits.debugPanel.extension

import com.fruits.debugPanel.RecommendationMockEditState
import com.fruits.domain.model.recommendations.Recommendations

fun Recommendations.toEditState() = RecommendationMockEditState(
    id = id,
    authorName = authorName,
    description = description,
    likesCount = likesCount.toString(),
    isLiked = isLiked
)

fun List<Recommendations>.toEditState() = map { it.toEditState() }