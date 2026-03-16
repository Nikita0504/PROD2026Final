package com.fruits.domain.model.interactions

data class IncomingLike(
    val likedByUserId: String,
    val firstName: String,
    val secondName: String,
    val age: Int,
    val city: String,
    val description: String,
    val photoFileKeys: List<String>,
    val createdAt: String,
)
