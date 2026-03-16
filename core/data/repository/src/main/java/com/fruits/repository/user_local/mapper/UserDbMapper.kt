package com.fruits.repository.user_local.mapper

import com.fruits.database.user.entity.UserEntity
import com.fruits.domain.model.user.User

object UserDbMapper {

    fun UserEntity.toDomain(): User =
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

    fun User.toEntity(): UserEntity =
        UserEntity(
            id = id,
            firstName = firstName,
            secondName = secondName,
            email = email,
            avatarFileKey = avatarFileKey,
            readyToGive = readyToGive,
            description = description,
            photoFileKeys = photoFileKeys
        )
}