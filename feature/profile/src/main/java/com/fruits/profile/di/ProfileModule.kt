package com.fruits.profile.di

import com.fruits.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel {
        ProfileViewModel(
            getProfileUseCase = get(),
            sessionManager = get(),
            imageUploadUrlRepository = get(),
        )
    }
}
