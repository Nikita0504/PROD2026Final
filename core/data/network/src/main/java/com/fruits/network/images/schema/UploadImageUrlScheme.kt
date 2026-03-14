package com.fruits.network.images.schema

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageUrlScheme(
    val url: String,
    val key: String
)