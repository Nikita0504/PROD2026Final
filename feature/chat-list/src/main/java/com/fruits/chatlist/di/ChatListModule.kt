package com.fruits.chatlist.di

import com.fruits.chatlist.ChatListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatListModule = module {
    viewModelOf(::ChatListViewModel)
}