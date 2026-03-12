package com.fruits.repository.di

import com.fruits.database.token.TokenStorage
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.repository.token.TokenRepositoryImpl
import com.fruits.repository.user_local.UserLocalRepositoryImpl
import com.fruits.repository.user_network.UserNetworkRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authRepositoryModule = module {
    single { TokenStorage(androidContext()) }
    singleOf(::TokenRepositoryImpl).bind<TokenRepository>()

    singleOf(::UserNetworkRepositoryImpl).bind<UserNetworkRepository>()
    singleOf(::UserLocalRepositoryImpl).bind<UserLocalRepository>()
}
