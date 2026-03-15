package com.fruits.domain.usecase.fcm

import com.fruits.domain.repository.FcmTokenRepository

class UpdateFcmTokenUseCase(
    private val repository: FcmTokenRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return repository.updateFcmToken(token)
    }
}
