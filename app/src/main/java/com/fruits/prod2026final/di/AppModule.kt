package com.fruits.prod2026final.di

import com.fruits.prod2026final.presentation.RootViewModel
import com.fruits.prod2026final.util.ShakeDetector
import com.fruits.session.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::RootViewModel)
    single {
        ShakeDetector(
            context = androidContext(),
            onShake = { get<SessionManager>().toggleDebug() }
        )
    }}
