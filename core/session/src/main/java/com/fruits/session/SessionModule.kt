package com.fruits.session

import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import com.fruits.domain.usecase.image.BatchDownloadImagesUseCase
import com.fruits.domain.usecase.image.UploadImageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sessionModule = module {
    singleOf(::LoginUseCase)

    single {
        GetProfileUseCase(
            get<UserNetworkRepository>(),
            get<UserLocalRepository>(),
            get<TokenRepository>()
        )
    }


    single {
        UploadImageUseCase(
            get(),
        )
    }

    single {
        UpdateProfileUseCase(
            get(),
            get(),
            get()
        )
    }


    single {
        BatchDownloadImagesUseCase(
            get(),
        )
    }

    singleOf(::SessionManager)

    singleOf(::GetRecommendationsUseCase)
}
