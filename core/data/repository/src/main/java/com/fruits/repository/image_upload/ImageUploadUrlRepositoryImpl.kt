package com.fruits.repository.image_upload

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.network.images.service.ImageUploadService
import com.fruits.network.util.ApiResult
import com.fruits.repository.image_upload.mapper.UploadingDataMapper.toDomain
import com.fruits.repository.util.mapResult
import com.fruits.repository.util.mockOr
import java.io.InputStream

class ImageUploadUrlRepositoryImpl(
    private val service: ImageUploadService,
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService
) : ImageUploadUrlRepository {

    override suspend fun getUploadUrl(): Result<UploadingData> =
        mockOr(mockStorage, { mockDataService.uploadingDataMock }) {
            service.getImageUploadService().mapResult { it.toDomain() }
        }

    override suspend fun uploadImage(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): Result<Unit> =
        mockOr(mockStorage, { Unit }) {
            service.uploadImageToUrl(url, inputStream, onProgress).mapResult { }
        }

    override suspend fun submitProfile(
        description: String,
        imageIds: List<String>
    ): Result<Unit> =
        mockOr(mockStorage, { Unit }) {
            ApiResult.Success(Unit).mapResult { }
        }
}
