package com.fruits.domain.model.recommendations

data class Recommendations(
    val userId: String,
    val firstName: String,
    val secondName: String,
    val age: Int,
    val city: String,
    val photoFileKeys: List<String>,
    val description: String,
    val explanation: List<String>,
    val tags: List<String>
)