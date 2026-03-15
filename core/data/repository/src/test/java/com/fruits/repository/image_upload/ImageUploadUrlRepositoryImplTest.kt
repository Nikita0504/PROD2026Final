package com.fruits.repository.image_upload

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.image.DownloadUrlData
import com.fruits.domain.model.image.UploadingData
import com.fruits.network.images.schema.DownloadUrlScheme
import com.fruits.network.images.schema.UploadImageUrlScheme
import com.fruits.network.images.service.ImageService
import com.fruits.network.util.ApiResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageUploadUrlRepositoryImplTest {

    @MockK
    private lateinit var service: ImageService

    @MockK
    private lateinit var mockStorage: MockStorage

    @MockK
    private lateinit var mockDataService: MockDataService

    private lateinit var repository: ImageUploadUrlRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = ImageUploadUrlRepositoryImpl(service, mockStorage, mockDataService)
    }

    @Test
    fun `getUploadUrl with mock disabled calls service and returns upload data`() = runTest {
        // Given
        val uploadUrlScheme = UploadImageUrlScheme(
            url = "https://example.com/upload",
            key = "image_key_123"
        )

        every { mockStorage.enabled } returns false
        coEvery { service.getUploadUrl() } returns ApiResult.Success(uploadUrlScheme)

        // When
        val result = repository.getUploadUrl()

        // Then
        assertTrue(result.isSuccess)
        val uploadingData = result.getOrNull()
        assertEquals("https://example.com/upload", uploadingData?.url)
        assertEquals("image_key_123", uploadingData?.key)
    }

    @Test
    fun `getUploadUrl with mock enabled returns mock data`() = runTest {
        // Given
        val mockUploadingData = UploadingData(
            url = "https://mock.com/upload",
            key = "mock_key"
        )

        every { mockStorage.enabled } returns true
        every { mockDataService.uploadingDataMock } returns mockUploadingData

        // When
        val result = repository.getUploadUrl()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("mock_key", result.getOrNull()?.key)
    }

    @Test
    fun `getUploadUrl when service returns error returns failure`() = runTest {
        // Given
        every { mockStorage.enabled } returns false
        coEvery { service.getUploadUrl() } returns ApiResult.Error("Service unavailable", 503)

        // When
        val result = repository.getUploadUrl()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getDownloadUrls returns list of DownloadUrlData`() = runTest {
        // Given
        val keys = listOf("key1", "key2")
        val schemes = listOf(
            DownloadUrlScheme(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlScheme(key = "key2", url = "https://example.com/img2.jpg")
        )

        coEvery { service.getDownloadUrls(keys) } returns ApiResult.Success(schemes)

        // When
        val result = repository.getDownloadUrls(keys)

        // Then
        assertTrue(result.isSuccess)
        val downloadData = result.getOrNull()
        assertEquals(2, downloadData?.size)
        assertEquals("key1", downloadData?.get(0)?.key)
        assertEquals("key2", downloadData?.get(1)?.key)
    }

    @Test
    fun `getDownloadUrls when service returns error returns failure`() = runTest {
        // Given
        val keys = listOf("key1")

        coEvery { service.getDownloadUrls(keys) } returns ApiResult.Error("Not found", 404)

        // When
        val result = repository.getDownloadUrls(keys)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getDownloadUrls with empty keys returns empty list`() = runTest {
        // Given
        val keys = emptyList<String>()
        val schemes = emptyList<DownloadUrlScheme>()

        coEvery { service.getDownloadUrls(keys) } returns ApiResult.Success(schemes)

        // When
        val result = repository.getDownloadUrls(keys)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `uploadImage calls service and returns success`() = runTest {
        // Given
        val url = "https://example.com/upload"
        val inputStream = mockk<InputStream>()
        val onProgress: (Float) -> Unit = {}

        coEvery { service.uploadImageToUrl(url, inputStream, any(), onProgress) } returns ApiResult.Success(Unit)

        // When
        val result = repository.uploadImage(url, inputStream, onProgress)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `uploadImage when service returns error returns failure`() = runTest {
        // Given
        val url = "https://example.com/upload"
        val inputStream = mockk<InputStream>()
        val onProgress: (Float) -> Unit = {}

        coEvery { service.uploadImageToUrl(url, inputStream, any(), onProgress) } returns ApiResult.Error("Upload failed", 500)

        // When
        val result = repository.uploadImage(url, inputStream, onProgress)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `downloadImages calls service and returns map of results`() = runTest {
        // Given
        val urls = mapOf("key1" to "https://example.com/img1.jpg")
        val imageResult = ApiResult.Success(byteArrayOf(1, 2, 3))

        coEvery { service.downloadImagesFromUrls(urls, any()) } returns mapOf("key1" to imageResult)

        // When
        val result = repository.downloadImages(urls)

        // Then
        assertEquals(1, result.size)
        assertTrue(result["key1"]?.isSuccess == true)
    }

    @Test
    fun `downloadImages when service returns error for key returns failure for that key`() = runTest {
        // Given
        val urls = mapOf("key1" to "https://example.com/img1.jpg")
        val imageResult = ApiResult.Error("Download failed", 500)

        coEvery { service.downloadImagesFromUrls(urls, any()) } returns mapOf("key1" to imageResult)

        // When
        val result = repository.downloadImages(urls)

        // Then
        assertEquals(1, result.size)
        assertTrue(result["key1"]?.isFailure == true)
    }

    @Test
    fun `submitProfile with mock disabled returns success`() = runTest {
        // Given
        val description = "Test description"
        val imageIds = listOf("img1", "img2")

        every { mockStorage.enabled } returns false

        // When
        val result = repository.submitProfile(description, imageIds)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `submitProfile with mock enabled returns success`() = runTest {
        // Given
        val description = "Test description"
        val imageIds = listOf("img1", "img2")

        every { mockStorage.enabled } returns true

        // When
        val result = repository.submitProfile(description, imageIds)

        // Then
        assertTrue(result.isSuccess)
    }
}
