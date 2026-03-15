package com.fruits.domain.model.image

data class SingleDownloadResult(
    val key: String,
    val imageData: ByteArray?,
    val error: Exception? = null,
    val progress: Float = 1f
) {
    val isSuccess: Boolean get() = error == null && imageData != null
}

data class BatchDownloadResult(
    val results: Map<String, SingleDownloadResult>,
    val totalKeys: Int,
    val successfulCount: Int,
    val failedCount: Int
) {
    val overallProgress: Float
        get() = if (totalKeys == 0) 0f else successfulCount.toFloat() / totalKeys

    val allSuccessful: Boolean
        get() = failedCount == 0

    fun getSuccessfulImages(): Map<String, ByteArray> {
        return results.filterValues { it.isSuccess }.mapValues { it.value.imageData!! }
    }

    fun getFailedKeys(): List<String> {
        return results.filterValues { !it.isSuccess }.keys.toList()
    }
}