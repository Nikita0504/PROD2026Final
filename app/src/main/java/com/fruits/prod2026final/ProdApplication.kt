package com.fruits.prod2026final

import android.app.Application
import com.fruits.chat.di.chatModule
import com.fruits.auth.di.authModule
import com.fruits.database.di.databaseModule
import com.fruits.debug.di.debugModule
import com.fruits.network.di.networkModule
import com.fruits.profile.di.profileModule
import com.fruits.prod2026final.di.appModule
import com.fruits.repository.di.authRepositoryModule
import com.fruits.register.di.registerModule
import com.fruits.session.sessionModule
import com.fruits.tape.di.tapeModule
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
                chatModule,
                authModule,
                registerModule,
                tapeModule,
                profileModule,
                if (BuildConfig.DEBUG) debugModule else emptyList()
            )
        }
    }
}
