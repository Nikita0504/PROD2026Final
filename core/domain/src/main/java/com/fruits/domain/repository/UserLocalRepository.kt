package com.fruits.domain.repository

import com.fruits.domain.model.user.User

interface UserLocalRepository {
    suspend fun getCachedUser(): User?
    suspend fun upsertUser(user: User)
    suspend fun clearCache()
}