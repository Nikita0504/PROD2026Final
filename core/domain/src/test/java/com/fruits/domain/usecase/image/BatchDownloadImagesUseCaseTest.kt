package com.fruits.domain.usecase.image

import com.fruits.domain.model.image.DownloadUrlData
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.logger.Log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchDownloadImagesUseCaseTest {

    @MockK
    private lateinit var repository: ImageUploadUrlRepository

    private lateinit var useCase: BatchDownloadImagesUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Мокаем объект Log чтобы избежать вызова android.util.Log
        mockkObject(Log)
        every { Log.d(any(), any()) } returns Unit
        every { Log.w(any(), any()) } returns Unit
        every { Log.e(any(), any()) } returns Unit
        every { Log.e(any(), any(), any()) } returns Unit
        every { Log.i(any(), any()) } returns Unit
        every { Log.v(any(), any()) } returns Unit

        useCase = BatchDownloadImagesUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkObject(Log)
    }

    @Test
    fun `invoke with empty fileKeys returns empty result`() = runTest {
        // Given
        val fileKeys = emptyList<String>()

        // When
        val result = useCase(fileKeys).first()

        // Then
        assertTrue(result.isSuccess)
        val batchResult = result.getOrNull()!!
        assertEquals(0, batchResult.totalKeys)
        assertEquals(0, batchResult.successfulCount)
        assertEquals(0, batchResult.failedCount)
        assertTrue(batchResult.results.isEmpty())
    }

    @Test
    fun `invoke when getDownloadUrls fails emits error`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2")
        val error = Exception("Failed to get download URLs")
        var progressCaptured = -1f
        val onProgress: (Float) -> Unit = { progress -> progressCaptured = progress }

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.failure(error)

        // When
        val result = useCase(fileKeys, onProgress).first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke with successful downloads returns all successful results`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg")
        )
        val imageData1 = byteArrayOf(1, 2, 3)
        val imageData2 = byteArrayOf(4, 5, 6)

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(imageData1),
            "key2" to Result.success(imageData2)
        )

        var progressCaptured = -1f
        val onProgress: (Float) -> Unit = { progress -> progressCaptured = progress }

        // When
        val result = useCase(fileKeys, onProgress).first()

        // Then
        assertTrue(result.isSuccess)
        val batchResult = result.getOrNull()!!
        assertEquals(2, batchResult.totalKeys)
        assertEquals(2, batchResult.successfulCount)
        assertEquals(0, batchResult.failedCount)
        assertEquals(2, batchResult.results.size)
        assertTrue(batchResult.results["key1"]?.isSuccess == true)
        assertTrue(batchResult.results["key2"]?.isSuccess == true)
        assertEquals(1f, progressCaptured)
    }

    @Test
    fun `invoke with partial failures returns mixed results`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2", "key3")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg"),
            DownloadUrlData(key = "key3", url = "https://example.com/img3.jpg")
        )
        val imageData1 = byteArrayOf(1, 2, 3)
        val error = Exception("Download failed")

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(imageData1),
            "key2" to Result.failure(error),
            "key3" to Result.success(byteArrayOf(7, 8, 9))
        )

        // When
        val result = useCase(fileKeys).first()

        // Then
        assertTrue(result.isSuccess)
        val batchResult = result.getOrNull()!!
        assertEquals(3, batchResult.totalKeys)
        assertEquals(2, batchResult.successfulCount)
        assertEquals(1, batchResult.failedCount)
        assertTrue(batchResult.results["key1"]?.isSuccess == true)
        assertFalse(batchResult.results["key2"]?.isSuccess == true)
        assertTrue(batchResult.results["key3"]?.isSuccess == true)
    }

    @Test
    fun `invoke with all failures returns all failed results`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg")
        )
        val error1 = Exception("Download failed 1")
        val error2 = Exception("Download failed 2")

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.failure(error1),
            "key2" to Result.failure(error2)
        )

        // When
        val result = useCase(fileKeys).first()

        // Then
        assertTrue(result.isSuccess) // Flow emits success, but results contain failures
        val batchResult = result.getOrNull()!!
        assertEquals(2, batchResult.totalKeys)
        assertEquals(0, batchResult.successfulCount)
        assertEquals(2, batchResult.failedCount)
        assertFalse(batchResult.results["key1"]?.isSuccess == true)
        assertFalse(batchResult.results["key2"]?.isSuccess == true)
    }

    @Test
    fun `invoke calls onFileProgress for each file`() = runTest {
        // Given
        val fileKeys = listOf("key1")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg")
        )
        val imageData = byteArrayOf(1, 2, 3)

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), captureLambda()) } answers {
            val onProgress = lambda<(String, Float) -> Unit>().captured
            onProgress.invoke("key1", 0.5f)
            onProgress.invoke("key1", 1f)
            mapOf("key1" to Result.success(imageData))
        }

        var fileProgressCaptured: Pair<String, Float>? = null
        val onFileProgress: (String, Float) -> Unit = { key, progress ->
            fileProgressCaptured = key to progress
        }

        // When
        useCase(fileKeys, onFileProgress = onFileProgress).first()

        // Then
        assertEquals("key1" to 1f, fileProgressCaptured)
    }

    @Test
    fun `invoke with single file returns correct progress calculation`() = runTest {
        // Given
        val fileKeys = listOf("key1")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg")
        )
        val imageData = byteArrayOf(1, 2, 3)

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), captureLambda()) } answers {
            val onProgress = lambda<(String, Float) -> Unit>().captured
            onProgress.invoke("key1", 0.5f)
            onProgress.invoke("key1", 1f)
            mapOf("key1" to Result.success(imageData))
        }

        val progressValues = mutableListOf<Float>()
        val onProgress: (Float) -> Unit = { progress -> progressValues.add(progress) }

        // When
        useCase(fileKeys, onProgress).first()

        // Then
        assertTrue(progressValues.isNotEmpty())
        assertEquals(1f, progressValues.last())
    }

    @Test
    fun `invoke returns batch result with correct getSuccessfulImages`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg")
        )
        val imageData1 = byteArrayOf(1, 2, 3)
        val imageData2 = byteArrayOf(4, 5, 6)

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(imageData1),
            "key2" to Result.success(imageData2)
        )

        // When
        val result = useCase(fileKeys).first()

        // Then
        val batchResult = result.getOrNull()!!
        val successfulImages = batchResult.getSuccessfulImages()
        assertEquals(2, successfulImages.size)
        assertTrue(successfulImages.containsKey("key1"))
        assertTrue(successfulImages.containsKey("key2"))
    }

    @Test
    fun `invoke returns batch result with correct getFailedKeys`() = runTest {
        // Given
        val fileKeys = listOf("key1", "key2", "key3")
        val downloadUrls = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg"),
            DownloadUrlData(key = "key3", url = "https://example.com/img3.jpg")
        )
        val error = Exception("Download failed")

        coEvery { repository.getDownloadUrls(fileKeys) } returns Result.success(downloadUrls)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(byteArrayOf(1, 2, 3)),
            "key2" to Result.failure(error),
            "key3" to Result.success(byteArrayOf(7, 8, 9))
        )

        // When
        val result = useCase(fileKeys).first()

        // Then
        val batchResult = result.getOrNull()!!
        val failedKeys = batchResult.getFailedKeys()
        assertEquals(1, failedKeys.size)
        assertEquals("key2", failedKeys.first())
    }

    @Test
    fun `invoke returns batch result with correct allSuccessful flag`() = runTest {
        // Given - all successful
        val fileKeys1 = listOf("key1", "key2")
        val downloadUrls1 = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg")
        )

        coEvery { repository.getDownloadUrls(fileKeys1) } returns Result.success(downloadUrls1)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(byteArrayOf(1, 2, 3)),
            "key2" to Result.success(byteArrayOf(4, 5, 6))
        )

        // When
        val result1 = useCase(fileKeys1).first()

        // Then
        val batchResult1 = result1.getOrNull()!!
        assertTrue(batchResult1.allSuccessful)

        // Given - with failures
        val fileKeys2 = listOf("key1", "key2")
        val downloadUrls2 = listOf(
            DownloadUrlData(key = "key1", url = "https://example.com/img1.jpg"),
            DownloadUrlData(key = "key2", url = "https://example.com/img2.jpg")
        )
        val error = Exception("Download failed")

        coEvery { repository.getDownloadUrls(fileKeys2) } returns Result.success(downloadUrls2)
        coEvery { repository.downloadImages(any(), any()) } returns mapOf(
            "key1" to Result.success(byteArrayOf(1, 2, 3)),
            "key2" to Result.failure(error)
        )

        // When
        val result2 = useCase(fileKeys2).first()

        // Then
        val batchResult2 = result2.getOrNull()!!
        assertFalse(batchResult2.allSuccessful)
    }
}
