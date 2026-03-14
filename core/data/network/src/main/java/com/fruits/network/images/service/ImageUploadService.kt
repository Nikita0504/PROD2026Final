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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ImageUploadService(
    private val client: HttpClient,
) {
    private val baseUrl = "${Const.serverUrl}/api/v1/files/presigned/upload_url"

    suspend fun getImageUploadService(): ApiResult<UploadImageUrlScheme> = safeCall {
        Log.d(TAG, "Requesting presigned upload URL from: $baseUrl")
        client
            .post(baseUrl) {
                jsonBody(Filename("logo"))
            }
            .toApiResult<UploadImageUrlScheme>()
            .also { result ->
                when (result) {
                    is ApiResult.Success -> Log.i(TAG, "Presigned URL obtained successfully: ${result.data.url}")
                    is ApiResult.Error   -> Log.w(TAG, "Failed to obtain presigned URL: code=${result.code}, message=${result.message}")
                }
            }
    }

    suspend fun uploadImageToUrl(
        url: String,
        inputStream: InputStream,
        onProgress: (Float) -> Unit
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeCall {
            val bytes = inputStream.readBytes()
            Log.d(TAG, "Image read: ${bytes.size} bytes")

            val connection = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                if (this is javax.net.ssl.HttpsURLConnection) {
                    val trust = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(c: Array<X509Certificate>, a: String) = Unit
                        override fun checkServerTrusted(c: Array<X509Certificate>, a: String) = Unit
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })
                    sslSocketFactory = SSLContext.getInstance("SSL")
                        .also { it.init(null, trust, SecureRandom()) }.socketFactory
                    hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
                requestMethod = "PUT"
                doOutput = true
                doInput = true
                setFixedLengthStreamingMode(bytes.size)
                setRequestProperty("Content-Type", "image/jpeg")
            }

            connection.outputStream.use { it.write(bytes) }

            val code = connection.responseCode
            val body = runCatching { connection.inputStream.bufferedReader().readText() }
                .getOrElse { connection.errorStream?.bufferedReader()?.readText() ?: "" }

            Log.d(TAG, "Upload response: $code, body: $body")
            onProgress(1f)

            if (code in 200..299) ApiResult.Success(Unit)
            else ApiResult.Error(body, code)
        }
    }


    private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    companion object {
        private const val TAG = "ImageUploadService"
    }
}
