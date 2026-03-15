package com.fruits.debugPanel.extension

import com.fruits.debugPanel.RecommendationMockEditState
import com.fruits.domain.model.recommendations.Recommendations

fun Recommendations.toEditState() = RecommendationMockEditState(
    userId = userId,
    firstName = firstName,
    secondName = secondName,
    age = age.toString(),
    city = city,
    description = description,
    explanation = explanation.joinToString("\n"),
)

fun List<Recommendations>.toEditState() = map { it.toEditState() }