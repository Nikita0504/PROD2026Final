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
}

