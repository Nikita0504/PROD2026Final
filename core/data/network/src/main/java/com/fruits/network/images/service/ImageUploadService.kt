package com.fruits.network.images.service

import android.util.Log
import com.fruits.network.Const
import com.fruits.network.images.schema.DownloadUrlScheme
import com.fruits.network.images.schema.DownloadUrlsRequest
import com.fruits.network.images.schema.DownloadUrlsResponse
import com.fruits.network.images.schema.Filename
import com.fruits.network.images.schema.UploadImageUrlScheme
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.InputStream

class ImageService(
    private val apiClient: HttpClient,
    private val rawClient: HttpClient
) {
    private companion object {
        private const val TAG = "ImageService"
        private const val UPLOAD_BASE_URL = "/api/v1/files/presigned/upload_url"
        private const val DOWNLOAD_BASE_URL = "/api/v1/files/presigned/download_urls"
    }

    suspend fun getUploadUrl(filename: String = "logo"): ApiResult<UploadImageUrlScheme> =
        safeCall {
            Log.d(TAG, "Getting upload URL for filename: $filename")

            apiClient
                .post("${Const.serverUrl}$UPLOAD_BASE_URL") {
                    jsonBody(Filename(filename))
                }
                .toApiResult<UploadImageUrlScheme>()
        }

    suspend fun uploadImageToUrl(
        url: String,
        inputStream: InputStream,
        contentType: String = "*/*",
        onProgress: (Float) -> Unit
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeCall {
            val bytes = inputStream.readBytes()
            Log.d(TAG, "Uploading ${bytes.size} bytes to: $url")

            val response = rawClient.put(url) {
                headers.remove(HttpHeaders.ContentType)
                headers.append(HttpHeaders.ContentType, contentType)

                header(HttpHeaders.ContentLength, bytes.size.toString())

                setBody(bytes)
            }

            onProgress(1f)
            Log.d(
                TAG,
                "Upload response: ${response.status}, body: ${response.bodyAsText().take(200)}"
            )
            response.toApiResult()
        }
    }

    suspend fun getDownloadUrls(keys: List<String>): ApiResult<List<DownloadUrlScheme>> = safeCall {
        Log.d(TAG, "Getting download URLs for ${keys.size} keys: $keys")

        val response = apiClient
            .post("${Const.serverUrl}$DOWNLOAD_BASE_URL") {
                contentType(ContentType.Application.Json)
                setBody(DownloadUrlsRequest(keys = keys))
            }.toApiResult<DownloadUrlsResponse>()

        when (response) {
            is ApiResult.Success<DownloadUrlsResponse> -> ApiResult.Success(
                response.data.urls
            )
            is ApiResult.Error -> response
        }
    }

    suspend fun downloadImageFromUrl(
        url: String,
        onProgress: (Float) -> Unit = {}
    ): ApiResult<ByteArray> = withContext(Dispatchers.IO) {
        safeCall {
            Log.d(TAG, "Downloading image from: $url")

            val response = rawClient.get(url)

            val bytes = response.bodyAsBytes()
            Log.d(TAG, "Downloaded ${bytes.size} bytes from: $url")

            onProgress(1f)
            ApiResult.Success(bytes)
        }
    }

    suspend fun downloadImagesFromUrls(
        urls: Map<String, String>,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ): Map<String, ApiResult<ByteArray>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting batch download for ${urls.size} images")

        if (urls.isEmpty()) {
            return@withContext emptyMap()
        }

        urls.map { (key, url) ->
            async {
                val result = downloadImageFromUrl(
                    url = url,
                    onProgress = { progress ->
                        onProgress(key, progress)
                    }
                )
                key to result
            }
        }.awaitAll().toMap()
    }

    private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}