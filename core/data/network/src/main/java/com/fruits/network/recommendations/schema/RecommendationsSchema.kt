package com.fruits.network.recommendations.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsResponse(
    val candidates: List<RecommendationsSchema>,
    @SerialName("al_used") val alUsed: Boolean = false
)

@Serializable
data class RecommendationsSchema(
    @SerialName("user_id")         val userId: String,
    @SerialName("first_name")      val firstName: String,
    @SerialName("second_name")     val secondName: String,
    val age: Int,
    val city: String,
    @SerialName("photo_file_keys") val photoFileKeys: List<String>,
    val description: String,
    val explanation: List<String>,
    val tags: List<String> = emptyList(),
)