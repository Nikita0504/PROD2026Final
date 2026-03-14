package com.fruits.network.images.service

import android.util.Log
import com.fruits.network.Const
import com.fruits.network.images.schema.Filename
import com.fruits.network.images.schema.UploadImageUrlScheme
import com.fruits.network.util.ApiResult
import com.fruits.network.util.safeCall
import com.fruits.network.util.toApiResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ImageUploadService(
    private val client: HttpClient,
    private val rawClient: HttpClient
) {
    private val baseUrl = "${Const.serverUrl}/api/v1/files/presigned/upload_url"

    suspend fun getImageUploadService(): ApiResult<UploadImageUrlScheme> = safeCall {
        Log.d("ImageUploadService", "Base url: $baseUrl")
        client
            .post(baseUrl) {
                jsonBody(Filename("logo"))
            }
            .toApiResult<UploadImageUrlScheme>()
    }

    suspend fun uploadImageToUrl(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeCall {
            val bytes = inputStream.readBytes()

            Log.d("ImageUploadService", "Uploading image to url: $url")

            val response = rawClient.put(url) {
                headers.remove(HttpHeaders.ContentType)
                headers.append(HttpHeaders.ContentType, "")
                header(HttpHeaders.ContentLength, bytes.size.toString())

                setBody(bytes)
                headers.remove(HttpHeaders.ContentType)
                headers.append(HttpHeaders.ContentType, "")
                Log.d("ImageUploadService", "Uploading headers: $headers")
            }
            onProgress(1f)
            Log.d("ImageUploadService", "Response after upload: ${response.bodyAsText()}")
            response.toApiResult()
        }
    }

    private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
