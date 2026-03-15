package com.fruits.repository.di

import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.repository.image_upload.ImageUploadUrlRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val imageUploadUrlRepositorModule = module {
    singleOf(::ImageUploadUrlRepositoryImpl).bind<ImageUploadUrlRepository>()
}
