package com.fruits.network.interactions.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncomingLikeSchema(
    @SerialName("liked_by_user_id") val likedByUserId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("second_name") val secondName: String,
    val age: Int,
    val city: String,
    val description: String = "",
    @SerialName("photo_file_keys") val photoFileKeys: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
)
