package com.fruits.audit.di

import com.fruits.audit.AuditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val auditModule = module {
    viewModel {
        AuditViewModel(getAuditEventsUseCase = get())
    }
}
