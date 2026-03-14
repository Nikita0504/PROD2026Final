package com.fruits.database.user.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fruits.database.user.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Int): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrent(): UserEntity?

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clear()
}