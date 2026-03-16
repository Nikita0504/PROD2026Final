package com.fruits.domain.usecase.image

import com.fruits.domain.repository.ImageUploadUrlRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream


class UploadImageUseCase(
    private val repository: ImageUploadUrlRepository
) {
    operator fun invoke(
        inputStream: InputStream
    ): Flow<Result<String>> = flow {
        val urlResult = repository.getUploadUrl()

        if (urlResult.isFailure) {
            emit(Result.failure(urlResult.exceptionOrNull() ?: Exception("Failed to get upload URL")))
            return@flow
        }

        val uploadData = urlResult.getOrNull()!!

        val uploadResult = repository.uploadImage(
            url = uploadData.url,
            inputStream = inputStream,
        )

        if (uploadResult.isFailure) {
            emit(Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed")))
            return@flow
        }

        emit(Result.success(uploadData.key))
    }
}