package com.fruits.repository.di

import com.fruits.domain.repository.FcmTokenRepository
import com.fruits.repository.fcm.FcmTokenRepositoryImpl
import org.koin.dsl.module

val fcmTokenRepositoryModule = module {
    single<FcmTokenRepository> { FcmTokenRepositoryImpl(get(), get()) }
}
