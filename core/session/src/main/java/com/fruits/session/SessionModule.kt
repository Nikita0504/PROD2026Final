package com.fruits.session

import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.domain.usecase.interactions.GetAuditEventsUseCase
import com.fruits.domain.usecase.interactions.GetIncomingLikesUseCase
import com.fruits.domain.usecase.interactions.ReportUserUseCase
import com.fruits.domain.usecase.interactions.SendUserActionUseCase
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.domain.usecase.auth.LoginUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.recommendations.GetRecommendationsUseCase
import com.fruits.domain.usecase.chat.DeleteChatUseCase
import com.fruits.domain.usecase.chat.GetChatUseCase
import com.fruits.domain.usecase.chat.ObserveChatsUseCase
import com.fruits.domain.usecase.chat.RefreshChatsUseCase
import com.fruits.domain.usecase.chat.SendChatMessageUseCase
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

    single {
        SessionManager(
            loginUseCase = get(),
            updateProfileUseCase = get(),
            fcmTokenProvider = get(),
            tokenRepository = get(),
            userLocalRepository = get(),
            userNetworkRepository = get()
        )
    }

    singleOf(::GetRecommendationsUseCase)
    singleOf(::GetIncomingLikesUseCase)
    singleOf(::GetAuditEventsUseCase)

    single {
        ObserveChatsUseCase(
            chatRepository = get(),
        )
    }

    single {
        RefreshChatsUseCase(
            chatRepository = get(),
            tokenRepository = get(),
        )
    }

    single {
        GetChatUseCase(
            chatRepository = get(),
            tokenRepository = get(),
        )
    }

    single {
        DeleteChatUseCase(
            chatRepository = get(),
            tokenRepository = get(),
        )
    }

    single {
        SendChatMessageUseCase(
            chatRepository = get(),
            tokenRepository = get(),
        )
    }

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
