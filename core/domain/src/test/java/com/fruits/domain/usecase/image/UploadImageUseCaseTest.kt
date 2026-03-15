package com.fruits.domain.usecase.image

import com.fruits.domain.repository.ImageUploadUrlRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadImageUseCaseTest {

    @MockK
    private lateinit var repository: ImageUploadUrlRepository

    @MockK
    private lateinit var inputStream: InputStream

    private lateinit var uploadImageUseCase: UploadImageUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        uploadImageUseCase = UploadImageUseCase(repository)
    }

    @Test
    fun `invoke with valid stream emits success with key`() = runTest {
        // Given
        val uploadUrl = "https://example.com/upload"
        val key = "image_key_123"
        val progressCallback: (Float) -> Unit = {}

        val uploadData = mockk<com.fruits.domain.model.image.UploadingData>(relaxed = true)
        every { uploadData.url } returns uploadUrl
        every { uploadData.key } returns key

        coEvery { repository.getUploadUrl() } returns Result.success(uploadData)
        coEvery { repository.uploadImage(uploadUrl, inputStream, any()) } returns Result.success(Unit)

        // When
        val result = uploadImageUseCase(inputStream, progressCallback).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(key, result.getOrNull())
    }

    @Test
    fun `invoke when getUploadUrl fails emits error`() = runTest {
        // Given
        val error = Exception("Failed to get upload URL")
        val progressCallback: (Float) -> Unit = {}

        coEvery { repository.getUploadUrl() } returns Result.failure(error)

        // When
        val result = uploadImageUseCase(inputStream, progressCallback).first()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke when upload fails emits error`() = runTest {
        // Given
        val uploadUrl = "https://example.com/upload"
        val key = "image_key"
        val uploadError = Exception("Upload failed")
        val progressCallback: (Float) -> Unit = {}

        val uploadData = mockk<com.fruits.domain.model.image.UploadingData>(relaxed = true)
        every { uploadData.url } returns uploadUrl
        every { uploadData.key } returns key

        coEvery { repository.getUploadUrl() } returns Result.success(uploadData)
        coEvery { repository.uploadImage(any(), any(), any()) } returns Result.failure(uploadError)

        // When
        val result = uploadImageUseCase(inputStream, progressCallback).first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(uploadError, result.exceptionOrNull())
    }

    @Test
    fun `invoke calls progress callback during upload`() = runTest {
        // Given
        val uploadUrl = "https://example.com/upload"
        val key = "image_key"
        var progressCaptured = -1f
        val progressCallback: (Float) -> Unit = { progress -> progressCaptured = progress }

        val uploadData = mockk<com.fruits.domain.model.image.UploadingData>(relaxed = true)
        every { uploadData.url } returns uploadUrl
        every { uploadData.key } returns key

        coEvery { repository.getUploadUrl() } returns Result.success(uploadData)
        coEvery { repository.uploadImage(any(), any(), captureLambda()) } answers {
            lambda<(Float) -> Unit>().captured.invoke(0.5f)
            Result.success(Unit)
        }

        // When
        uploadImageUseCase(inputStream, progressCallback).first()

        // Then
        assertEquals(0.5f, progressCaptured)
    }
}
