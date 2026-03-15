package com.fruits.database.user.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "second_name") val secondName: String,
    val email: String,
    @ColumnInfo(name = "avatar_file_key") val avatarFileKey: String?,
    @ColumnInfo(name = "ready_to_give") val readyToGive: Boolean,
    @ColumnInfo(name = "photo_file_keys") val photoFileKeys: List<String>,
    val description: String?
)