package com.fruits.settings.di

import com.fruits.settings.ProfileSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { ProfileSettingsViewModel(get(), get(), get(), get(), get()) }
}

