package com.fruits.repository.user_network.mapper

import com.fruits.domain.model.user.User
import com.fruits.network.user.schema.UserCreateSchema
import com.fruits.network.user.schema.UserReadSchema

object UserMapper {
    fun UserReadSchema.toDomain(): User =
        User(
            id = id,
            firstName = firstName,
            secondName = secondName,
            email = email,
            avatarFileKey = avatarFileKey,
            readyToGive = readyToGive,
            description = description,
            photoFileKeys = photoFileKeys
        )

    fun User.toSchema(): UserCreateSchema =
        UserCreateSchema(
            firstName = firstName,
            secondName = secondName,
            email = email,
            password = "",
            readyToGive = readyToGive,
        )
}