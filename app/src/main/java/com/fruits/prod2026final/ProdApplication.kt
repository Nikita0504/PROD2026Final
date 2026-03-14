package com.fruits.prod2026final

import android.app.Application
import com.fruits.auth.di.authModule
import com.fruits.chat.di.chatModule
import com.fruits.database.di.databaseModule
import com.fruits.debug.di.debugModule
import com.fruits.debugPanel.di.debugPanelModule
import com.fruits.network.di.networkModule
import com.fruits.onboarding.di.onboardingModule
import com.fruits.prod2026final.di.appModule
import com.fruits.profile.di.profileModule
import com.fruits.repository.di.authRepositoryModule
import com.fruits.repository.di.imageUploadUrlRepositorModule
import com.fruits.session.sessionModule
import com.fruits.tape.di.tapeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlin.collections.emptyList

class ProdApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ProdApplication)
            modules(
                databaseModule,
                networkModule,
                authRepositoryModule,
                sessionModule,
                appModule,
                chatModule,
                authModule,
                onboardingModule,
                imageUploadUrlRepositorModule,
                tapeModule,
                profileModule,
                debugModule, //if (BuildConfig.DEBUG)
                debugPanelModule
            )
        }
    }
}
