package com.fruits.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fruits.database.user.dao.UserDao
import com.fruits.database.user.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        const val NAME: String = "prod2026final.db"
    }
}

