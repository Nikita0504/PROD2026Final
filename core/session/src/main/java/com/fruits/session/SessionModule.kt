package com.fruits.session

import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.fcm.UpdateFcmTokenUseCase
import com.fruits.domain.usecase.interactions.ReportUserUseCase
import com.fruits.domain.usecase.interactions.SendUserActionUseCase
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
    singleOf(::UpdateFcmTokenUseCase)

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

    single {
        SessionManager(
            loginUseCase = get(),
            updateProfileUseCase = get(),
            updateFcmTokenUseCase = get(),
            fcmTokenProvider = get(),
            tokenRepository = get(),
            userLocalRepository = get(),
            userNetworkRepository = get()
        )
    }

    singleOf(::GetRecommendationsUseCase)

    single {
        SendUserActionUseCase(
            interactionsRepository = get(),
            tokenRepository = get(),
        )
    }

    single {
        ReportUserUseCase(
            interactionsRepository = get(),
            tokenRepository = get(),
        )
    }
}
