package com.fruits.repository.recommendations.mapper

import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.network.recommendations.schema.RecommendationsSchema

object RecommendationsMapper {
    fun RecommendationsSchema.toDomain(): Recommendations = Recommendations(
        userId = userId,
        firstName = firstName,
        secondName = secondName,
        age = age,
        city = city,
        photoFileKeys = photoFileKeys,
        description = description,
        explanation = explanation
    )

    fun List<RecommendationsSchema>.toDomain(): List<Recommendations> = map { it.toDomain() }
}