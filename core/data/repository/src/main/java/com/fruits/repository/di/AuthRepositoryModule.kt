package com.fruits.repository.di

import com.fruits.domain.repository.AuthRepository
import com.fruits.repository.auth.AuthRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authRepositoryModule = module {
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
}
