package com.fruits.domain.usecase.auth

import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate
import com.fruits.domain.repository.TokenRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.repository.UserNetworkRepository

class UpdateProfileUseCase(
    private val userNetworkRepository: UserNetworkRepository,
    private val tokensRepository: TokenRepository,
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(
        description: String,
        photoFilesKeys: List<String>,
    ): Result<User> {
        val accessToken = tokensRepository.getAccessToken()

        val updateData = UserProfileUpdate(
            photoFilesKeys = photoFilesKeys,
            description = description
        )

        val result = userNetworkRepository.patchProfile(updateData, accessToken)

        return result.onSuccess { user -> userLocalRepository.upsertUser(user) }
    }
}