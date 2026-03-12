package com.fruits.database.di

import androidx.room.Room
import com.fruits.database.AppDatabase
import com.fruits.database.user.dao.UserDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.NAME
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single<UserDao> {
        get<AppDatabase>().userDao()
    }
}

