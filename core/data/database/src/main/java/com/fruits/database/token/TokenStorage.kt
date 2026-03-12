package com.fruits.database.token

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
@Suppress("DEPRECATION")
class TokenStorage(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        "secure_tokens",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
        }
    }

    fun getAccessToken(): String = prefs.getString(KEY_ACCESS, "") ?: ""

    fun getRefreshToken(): String = prefs.getString(KEY_REFRESH, "") ?: ""

    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS)
                .remove(KEY_REFRESH)
        }
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }
}
