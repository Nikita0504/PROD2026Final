package com.fruits.debug.di

import com.fruits.debug.DebugNetworkLogger
import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.network.user.logger.NetworkEventLogger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val debugModule = module {
    singleOf(::DebugNetworkLogger).bind<NetworkEventLogger>()
    singleOf(::MockStorage)
    singleOf(::MockDataService)
}