package com.fruits.domain.repository

interface FcmTokenRepository {
    suspend fun updateFcmToken(token: String): Result<Unit>
}
