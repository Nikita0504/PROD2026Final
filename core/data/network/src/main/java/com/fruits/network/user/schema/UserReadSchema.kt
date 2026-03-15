package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserReadSchema(
    @SerialName("first_name") val firstName: String,
    @SerialName("second_name") val secondName: String,
    val email: String,
    val id: String,
    @SerialName("avatar_file_key") val avatarFileKey: String? = null,
    @SerialName("ready_to_give") val readyToGive: Boolean,
    val description: String? = null,
    @SerialName("photo_file_keys") val photoFileKeys: List<String> = listOf()
)