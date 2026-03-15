package com.fruits.prod2026final

import android.app.Application
import coil3.ImageLoader
import com.fruits.auth.di.authModule
import com.fruits.chat.di.chatModule
import com.fruits.chatlist.di.chatListModule
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import com.fruits.database.di.databaseModule
import com.fruits.debug.di.debugModule
import com.fruits.debugPanel.di.debugPanelModule
import com.fruits.logger.di.loggerModule
import com.fruits.network.di.networkModule
import com.fruits.onboarding.di.onboardingModule
import com.fruits.prod2026final.di.appModule
import com.fruits.profile.di.profileModule
import com.fruits.repository.di.authRepositoryModule
import com.fruits.repository.di.fcmTokenRepositoryModule
import com.fruits.repository.di.imageUploadUrlRepositorModule
import com.fruits.session.sessionModule
import com.fruits.tape.di.tapeModule
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ProdApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initCoilImageLoader()
        startKoin {
            androidContext(this@ProdApplication)
            modules(
                databaseModule,
                networkModule,
                authRepositoryModule,
                fcmTokenRepositoryModule,
                sessionModule,
                appModule,
                authModule,
                onboardingModule,
                imageUploadUrlRepositorModule,
                tapeModule,
                chatListModule,
                chatModule,
                profileModule,
                debugModule,
                debugPanelModule,
                loggerModule
            )
        }
    }

    private fun initCoilImageLoader() {
        SingletonImageLoader.setSafe {
            val okHttpClient = if (BuildConfig.DEBUG) {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                val sslContext = SSLContext.getInstance("SSL").apply {
                    init(null, trustAllCerts, SecureRandom())
                }
                OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
                    .build()
            } else {
                OkHttpClient()
            }
            ImageLoader.Builder(this@ProdApplication)
                .serviceLoaderEnabled(false)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
                }
                .build()
        }
    }
}
