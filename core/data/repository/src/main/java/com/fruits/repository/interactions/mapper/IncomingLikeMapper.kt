package com.fruits.repository.interactions.mapper

import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.network.interactions.schema.IncomingLikeSchema

fun IncomingLikeSchema.toDomain(): IncomingLike = IncomingLike(
    likedByUserId = likedByUserId,
    firstName = firstName,
    secondName = secondName,
    age = age,
    city = city,
    description = description,
    photoFileKeys = photoFileKeys,
    createdAt = createdAt,
)
