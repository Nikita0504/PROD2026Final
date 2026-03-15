package com.fruits.network.images.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadUrlScheme(
    @SerialName("key")
    val key: String,
    @SerialName("url")
    val url: String
)

@Serializable
data class DownloadUrlsResponse(
    @SerialName("urls")
    val urls: List<DownloadUrlScheme>
)

@Serializable
data class DownloadUrlsRequest(
    @SerialName("keys")
    val keys: List<String>
)