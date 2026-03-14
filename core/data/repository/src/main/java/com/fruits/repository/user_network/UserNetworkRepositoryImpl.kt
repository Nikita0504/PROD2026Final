package com.fruits.repository.user_network

import com.fruits.domain.model.user.AuthResult
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.network.user.schema.UserCreateSchema
import com.fruits.network.user.schema.UserLoginSchema
import com.fruits.network.user.schema.UserProfileUpdateScheme
import com.fruits.network.user.service.UserService
import com.fruits.repository.user_network.mapper.AuthMapper.toDomain
import com.fruits.repository.user_network.mapper.TokensMapper.toDomain
import com.fruits.repository.user_network.mapper.UserMapper.toDomain
import com.fruits.repository.util.mapResult

class UserNetworkRepositoryImpl(
    private val service: UserService
) : UserNetworkRepository {

    override suspend fun register(
        firstName: String,
        secondName: String,
        email: String,
        password: String
    ): Result<AuthResult> =
        service.register(UserCreateSchema(firstName, secondName, email, password))
            .mapResult { it.toDomain() }

    override suspend fun login(email: String, password: String): Result<Tokens> =
        service.login(UserLoginSchema(email, password))
            .mapResult { it.toDomain() }

    override suspend fun getProfile(accessToken: String): Result<User> =
        service.getProfile(accessToken).mapResult { it.toDomain() }

    override suspend fun refreshToken(refreshToken: String): Result<Tokens> =
        service.refreshToken(refreshToken).mapResult { it.toDomain() }

    override suspend fun patchProfile(
        updateData: UserProfileUpdate,
        accessToken: String
    ): Result<User> =
        service.patchProfile(UserProfileUpdateScheme(
            updateData.description,
            updateData.photoFilesKeys
        ), accessToken).mapResult {
            it.toDomain()
        }
}
