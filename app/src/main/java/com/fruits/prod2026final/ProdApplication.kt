package com.fruits.prod2026final

import android.app.Application
import com.fruits.auth.di.authModule
import com.fruits.database.di.databaseModule
import com.fruits.network.di.networkModule
import com.fruits.onboarding.di.onboardingModule
import com.fruits.prod2026final.di.appModule
import com.fruits.repository.di.authRepositoryModule
import com.fruits.repository.di.imageUploadUrlRepositorModule
import com.fruits.session.sessionModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

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
                authModule,
                onboardingModule,
                imageUploadUrlRepositorModule
            )
        }
    }
}
