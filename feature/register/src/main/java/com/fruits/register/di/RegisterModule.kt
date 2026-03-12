package com.fruits.register.di

import com.fruits.register.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val registerModule = module {
    viewModel { RegisterViewModel(get()) }
}

