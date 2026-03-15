package com.fruits.prod2026final.fcm

import com.fruits.domain.repository.FcmTokenProvider
import com.fruits.logger.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseTokenProvider(
    private val firebaseMessaging: FirebaseMessaging
) : FcmTokenProvider {

    override suspend fun getFcmToken(): String? {
        return try {
            val token = firebaseMessaging.token.await()
            Log.d(TAG, "FCM token retrieved: ${token?.take(20)}...")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    companion object {
        private const val TAG = "FirebaseTokenProvider"
    }
}
