package com.fruits.domain.repository

import com.fruits.domain.model.image.UploadingData
import java.io.InputStream

interface ImageUploadUrlRepository {
    suspend fun getUploadUrl(): Result<UploadingData>
    suspend fun uploadImage(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): Result<Unit>
    suspend fun submitProfile(description: String, imageIds: List<String>): Result<Unit>
}