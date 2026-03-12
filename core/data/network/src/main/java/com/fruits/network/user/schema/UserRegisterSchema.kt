package com.fruits.network.user.schema

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterSchema(
    val user: UserReadSchema,
    val tokens: TokenReadSchema
)