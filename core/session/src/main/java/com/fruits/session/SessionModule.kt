package com.fruits.session

import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import com.fruits.domain.usecase.uploading.UploadImageUseCase
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

    singleOf(::UploadImageUseCase)

    singleOf (::UpdateProfileUseCase)

    singleOf(::SessionManager)

    singleOf(::GetRecommendationsUseCase)
}
