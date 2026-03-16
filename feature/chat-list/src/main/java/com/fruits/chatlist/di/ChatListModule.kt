package com.fruits.chatlist.di

import com.fruits.chatlist.ChatListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatListModule = module {
    viewModel {
        ChatListViewModel(
            observeChatsUseCase = get(),
            refreshChatsUseCase = get(),
            imageUploadUrlRepository = get(),
        )
    }
}