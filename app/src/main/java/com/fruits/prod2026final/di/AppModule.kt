package com.fruits.prod2026final.di

import com.fruits.prod2026final.presentation.RootViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { RootViewModel(get()) }
}
