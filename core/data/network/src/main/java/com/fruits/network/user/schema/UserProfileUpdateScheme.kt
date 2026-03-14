package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileUpdateScheme(
    val description: String,
    @SerialName("photo_file_keys") val photoFilesKeys: List<String>
)