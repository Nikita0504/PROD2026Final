package com.fruits.domain.model.image

data class DownloadUrlData(
    val key: String,
    val url: String,
    val contentType: String? = null,
    val fileSize: Long? = null
)