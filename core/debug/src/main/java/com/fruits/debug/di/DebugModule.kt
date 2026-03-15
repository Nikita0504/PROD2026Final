package com.fruits.debug.di

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val debugModule = module {
    singleOf(::MockStorage)
    singleOf(::MockDataService)
}