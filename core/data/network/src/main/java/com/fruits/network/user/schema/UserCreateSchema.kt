package com.fruits.network.user.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateSchema(
    @SerialName("first_name") val firstName: String,
    @SerialName("second_name") val secondName: String,
    val email: String,
    val password: String,
    @SerialName("avatar_file_key") val avatarFileKey: String? = null
)