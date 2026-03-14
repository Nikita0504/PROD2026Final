package com.fruits.repository.image_upload

import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.network.images.service.ImageUploadService
import com.fruits.network.util.ApiResult
import com.fruits.repository.image_upload.mapper.UploadingDataMapper.toDomain
import com.fruits.repository.util.mapResult
import java.io.InputStream

class ImageUploadUrlRepositoryImpl(
    private val service: ImageUploadService,
) : ImageUploadUrlRepository {
    override suspend fun getUploadUrl(): Result<UploadingData> =
        service.getImageUploadService().mapResult { it.toDomain() }

    override suspend fun uploadImage(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): Result<Unit> = service.uploadImageToUrl(
        url,
        inputStream,
        onProgress
    ).mapResult { }


    override suspend fun submitProfile(
        description: String,
        imageIds: List<String>
    ): Result<Unit> {
        return ApiResult.Success(Unit).mapResult {}
    }
}
