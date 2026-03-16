package com.fruits.domain.usecase.image

import com.fruits.logger.Log
import com.fruits.domain.model.image.BatchDownloadResult
import com.fruits.domain.model.image.SingleDownloadResult
import com.fruits.domain.repository.ImageUploadUrlRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BatchDownloadImagesUseCase(
    private val repository: ImageUploadUrlRepository
) {
    private companion object {
        private const val TAG = "BatchDownloadUseCase"
    }

    operator fun invoke(
        fileKeys: List<String>,
        onProgress: (Float) -> Unit = {},
        onFileProgress: (String, Float) -> Unit = { _, _ -> }
    ): Flow<Result<BatchDownloadResult>> = flow {
        Log.d(TAG, "Starting batch download for ${fileKeys.size} files: $fileKeys")

        if (fileKeys.isEmpty()) {
            Log.w(TAG, "Empty fileKeys list provided")
            emit(Result.success(
                BatchDownloadResult(
                    results = emptyMap(),
                    totalKeys = 0,
                    successfulCount = 0,
                    failedCount = 0
                )
            ))
            return@flow
        }

        Log.d(TAG, "Fetching presigned URLs for ${fileKeys.size} keys")
        val urlsResult = repository.getDownloadUrls(fileKeys)

        if (urlsResult.isFailure) {
            val error = urlsResult.exceptionOrNull() ?: Exception("Failed to get download URLs")
            Log.e(TAG, "Failed to get download URLs: ${error.message}", error)
            emit(Result.failure(error))
            return@flow
        }

        val downloadUrls = urlsResult.getOrNull()!!
        Log.d(TAG, "Successfully got ${downloadUrls.size} presigned URLs")

        val urlMap = downloadUrls.associate { it.key to it.url }

        Log.d(TAG, "Starting to download ${urlMap.size} images")

        val downloadResults = repository.downloadImages(
            urls = urlMap,
            onProgress = { key, progress ->
                Log.d(TAG, "File $key download progress: ${(progress * 100).toInt()}%")
                onFileProgress(key, progress)

                val urlFetchProgress = 0.1f
                val downloadProgress = 0.9f / urlMap.size
                val currentProgress = urlFetchProgress + (downloadProgress * progress)
                onProgress(currentProgress.coerceIn(0f, 1f))
            }
        )

        val resultsMap = mutableMapOf<String, SingleDownloadResult>()
        var successfulCount = 0
        var failedCount = 0

        downloadResults.forEach { (key, result) ->
            result.onSuccess { imageData ->
                Log.d(TAG, "Successfully downloaded $key: ${imageData.size} bytes")
                resultsMap[key] = SingleDownloadResult(
                    key = key,
                    imageData = imageData,
                    error = null,
                    progress = 1f
                )
                successfulCount++
            }.onFailure { error ->
                Log.e(TAG, "Failed to download $key: ${error.message}", error)
                resultsMap[key] = SingleDownloadResult(
                    key = key,
                    imageData = null,
                    error = error as Exception?,
                    progress = 0f
                )
                failedCount++
            }
        }

        val batchResult = BatchDownloadResult(
            results = resultsMap,
            totalKeys = fileKeys.size,
            successfulCount = successfulCount,
            failedCount = failedCount
        )

        Log.d(
            TAG,
            "Batch download completed: ${successfulCount}/${fileKeys.size} successful, $failedCount failed"
        )

        onProgress(1f)

        emit(Result.success(batchResult))
    }
}