package com.fruits.session

import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.RegisterUserUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sessionModule = module {
    single {
        LoginUseCase(
            get<UserNetworkRepository>(),
            get<UserLocalRepository>(),
            get<TokenRepository>()
        )
    }

    single {
        RegisterUserUseCase(
            get<UserNetworkRepository>(),
            get<UserLocalRepository>(),
            get<TokenRepository>()
        )
    }

    singleOf(::SessionManager)
}
