package com.fruits.auth.di

import com.fruits.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel { AuthViewModel(get()) }
}
