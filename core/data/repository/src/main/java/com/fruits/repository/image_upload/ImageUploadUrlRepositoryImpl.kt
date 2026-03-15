package com.fruits.repository.image_upload

import com.fruits.domain.model.image.DownloadUrlData
import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.network.images.service.ImageService
import com.fruits.network.util.ApiResult
import com.fruits.repository.image_upload.mapper.UploadingDataMapper.toDomain
import com.fruits.repository.util.mapResult
import java.io.InputStream

class ImageUploadUrlRepositoryImpl(
    private val service: ImageService,
) : ImageUploadUrlRepository {
    override suspend fun getUploadUrl(): Result<UploadingData> =
        service.getUploadUrl().mapResult { it.toDomain() }

    override suspend fun uploadImage(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): Result<Unit> = service.uploadImageToUrl(
        url,
        inputStream,
        onProgress = onProgress
    ).mapResult { }

    override suspend fun getDownloadUrls(keys: List<String>): Result<List<DownloadUrlData>> {
        return service.getDownloadUrls(keys).mapResult { schemes ->
            schemes.map { scheme ->
                DownloadUrlData(
                    key = scheme.key,
                    url = scheme.url,
                    contentType = null,
                    fileSize = null
                )
            }
        }
    }

    override suspend fun downloadImages(
        urls: Map<String, String>,
        onProgress: (String, Float) -> Unit
    ): Map<String, Result<ByteArray>> {
        return service.downloadImagesFromUrls(
            urls = urls,
            onProgress = onProgress
        ).mapValues { (_, apiResult) ->
            apiResult.mapResult { it }
        }
    }

    override suspend fun submitProfile(
        description: String,
        imageIds: List<String>
    ): Result<Unit> {
        return ApiResult.Success(Unit).mapResult {}
    }
}
