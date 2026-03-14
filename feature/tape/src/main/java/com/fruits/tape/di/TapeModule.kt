package com.fruits.tape.di

import com.fruits.tape.TapeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tapeModule = module {
    viewModelOf (::TapeViewModel)
}
