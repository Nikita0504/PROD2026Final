package com.fruits.debugPanel.di

import com.fruits.debugPanel.DebugPanelViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val debugPanelModule = module {
    viewModelOf(::DebugPanelViewModel)
}