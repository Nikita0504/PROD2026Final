package com.fruits.domain.repository

import com.fruits.domain.model.image.DownloadUrlData
import com.fruits.domain.model.image.UploadingData
import java.io.InputStream

interface ImageUploadUrlRepository {
    suspend fun getUploadUrl(): Result<UploadingData>
    suspend fun uploadImage(
        url: String,
        inputStream: InputStream,
    ): Result<Unit>

    suspend fun getDownloadUrls(keys: List<String>): Result<List<DownloadUrlData>>

    suspend fun downloadImages(
        urls: Map<String, String>,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ): Map<String, Result<ByteArray>>

    suspend fun submitProfile(description: String, imageIds: List<String>): Result<Unit>
}