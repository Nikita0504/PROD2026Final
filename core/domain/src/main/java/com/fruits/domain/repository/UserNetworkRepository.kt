package com.fruits.domain.repository

import android.net.Uri
import com.fruits.domain.model.user.AuthResult
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate

interface UserNetworkRepository {

    suspend fun login(email: String, password: String): Result<Tokens>
    suspend fun getProfile(accessToken: String): Result<User>

    suspend fun refreshToken(refreshToken: String): Result<Tokens>

    suspend fun patchProfile(updateData: UserProfileUpdate, accessToken: String): Result<User>
}
