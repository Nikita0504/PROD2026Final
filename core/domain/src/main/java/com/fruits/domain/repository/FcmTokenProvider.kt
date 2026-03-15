package com.fruits.domain.repository

interface FcmTokenProvider {
    suspend fun getFcmToken(): String?
}
