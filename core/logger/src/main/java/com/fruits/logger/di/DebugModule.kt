package com.fruits.logger.di

import com.fruits.logger.DebugNetworkLogger
import com.fruits.logger.NetworkEventLogger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loggerModule = module {
    singleOf(::DebugNetworkLogger).bind<NetworkEventLogger>()
}