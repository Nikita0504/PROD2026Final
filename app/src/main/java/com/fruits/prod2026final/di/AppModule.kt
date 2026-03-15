package com.fruits.prod2026final.di

import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.domain.usecase.fcm.UpdateFcmTokenUseCase
import com.fruits.prod2026final.fcm.FirebaseTokenProvider
import com.fruits.prod2026final.presentation.RootViewModel
import com.fruits.prod2026final.util.ShakeDetector
import com.fruits.repository.fcm.FcmTokenRepositoryImpl
import com.fruits.session.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::RootViewModel)
    single {
        ShakeDetector(
            context = androidContext(),
            onShake = { get<SessionManager>().toggleDebug() }
        )
    }

    // Firebase
    single { FirebaseMessaging.getInstance() }
    single<FcmTokenProvider> { FirebaseTokenProvider(get()) }

    // FCM Token UseCase (Repository регистрируется в fcmTokenRepositoryModule)
    single { UpdateFcmTokenUseCase(get()) }
}
