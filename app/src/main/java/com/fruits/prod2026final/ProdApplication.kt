package com.fruits.prod2026final

import android.app.Application
import com.fruits.auth.di.authModule
import com.fruits.prod2026final.di.appModule
import com.fruits.repository.di.authRepositoryModule
import com.fruits.session.sessionModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ProdApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ProdApplication)
            modules(authRepositoryModule, sessionModule, appModule, authModule)
        }
    }
}
