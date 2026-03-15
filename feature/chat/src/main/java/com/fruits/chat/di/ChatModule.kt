package com.fruits.chat.di

import androidx.lifecycle.SavedStateHandle
import com.fruits.chat.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    viewModel { (handle: SavedStateHandle) ->
        ChatViewModel(
            savedStateHandle = handle,
            getChatUseCase = get(),
            sendChatMessageUseCase = get(),
        )
    }
}
