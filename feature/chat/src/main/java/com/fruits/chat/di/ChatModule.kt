package com.fruits.chat.di

import com.fruits.chat.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    viewModel { ChatViewModel() }
}
