package com.fruits.network.di

import com.fruits.network.user.service.UserService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.* // Import all logging classes
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.fruits.network.BuildConfig
import com.fruits.network.images.service.ImageUploadService
import android.util.Log // Android Log utility
import org.koin.core.qualifier.named

val networkModule = module {

    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                // Use Android Logcat logger
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }

                // LEVEL_ALL ensures you get: Method, URL, Headers, and Body
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE

                // Optional: Customize what is logged if ALL is too verbose later
                // filter { FilterCondition... }

                // Sanitize sensitive headers if needed (e.g., Authorization)
                // sanitizeHeader { header -> header == "Authorization" }
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 2)
                exponentialDelay()
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }

            expectSuccess = false
        }
    }

    single(named("rawClient")) {
        HttpClient(Android) {
            // ❌ No ContentNegotiation plugin here!
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000 // Longer for uploads
            }
            expectSuccess = false
        }
    }

    singleOf(::UserService)
    single { ImageUploadService(get(), get(named("rawClient"))) }
}