package com.fruits.network.di

import com.fruits.network.user.service.UserService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.fruits.network.BuildConfig
import com.fruits.network.images.service.ImageUploadService
import com.fruits.network.user.logger.NetworkEventLogger
import com.fruits.network.user.logger.NoOpNetworkLogger
import java.util.logging.Logger

val networkModule = module {

    single<NetworkEventLogger> { NoOpNetworkLogger }

    single {
        val eventLogger: NetworkEventLogger = get()

        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        eventLogger.log("HTTP", message)
                    }
                }
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 2)
                exponentialDelay()
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis  = 15_000
            }

            expectSuccess = false
        }
    }

    singleOf(::UserService)
    singleOf(::ImageUploadService)
}
