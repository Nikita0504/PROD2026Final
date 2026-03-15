package com.fruits.repository.fcm

import com.fruits.domain.repository.FcmTokenRepository
import com.fruits.domain.repository.TokenRepository
import com.fruits.logger.Log
import com.fruits.network.user.service.UserService
import com.fruits.network.util.ApiResult

class FcmTokenRepositoryImpl(
    private val userService: UserService,
    private val tokenRepository: TokenRepository
) : FcmTokenRepository {

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        val accessToken = tokenRepository.getAccessToken()

        if (accessToken.isEmpty()) {
            Log.w(TAG, "Cannot update FCM token: no access token")
            return Result.failure(IllegalStateException("User not authorized"))
        }

        return when (val result = userService.updateFcmToken(token, accessToken)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception("[${result.code}] ${result.message}"))
        }
    }

    companion object {
        private const val TAG = "FcmTokenRepository"
    }
}
