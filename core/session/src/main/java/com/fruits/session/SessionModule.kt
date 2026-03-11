package com.fruits.session

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sessionModule = module {
    singleOf(::SessionManager)
}
