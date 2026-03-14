package com.fruits.database.token

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import com.fruits.debug.Log

@Suppress("DEPRECATION")
class TokenStorage(context: Context) {

    private val prefs = runCatching {
        EncryptedSharedPreferences.create(
            "secure_tokens",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).also {
            Log.i(TAG, "EncryptedSharedPreferences initialized successfully")
        }
    }.onFailure { e ->
        Log.e(TAG, "Failed to initialize EncryptedSharedPreferences", e)
    }.getOrThrow()

    fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d(TAG, "Saving tokens: accessToken present=${accessToken.isNotBlank()}, refreshToken present=${refreshToken.isNotBlank()}")
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
        }
        Log.i(TAG, "Tokens saved successfully")
    }

    fun getAccessToken(): String {
        val token = prefs.getString(KEY_ACCESS, "") ?: ""
        Log.d(TAG, "Access token retrieved: present=${token.isNotBlank()}")
        return token
    }

    fun getRefreshToken(): String {
        val token = prefs.getString(KEY_REFRESH, "") ?: ""
        Log.d(TAG, "Refresh token retrieved: present=${token.isNotBlank()}")
        return token
    }

    fun clearTokens() {
        Log.w(TAG, "Clearing all tokens from secure storage")
        prefs.edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
        Log.i(TAG, "Tokens cleared successfully")
    }

    companion object {
        private const val TAG = "TokenStorage"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }
}
