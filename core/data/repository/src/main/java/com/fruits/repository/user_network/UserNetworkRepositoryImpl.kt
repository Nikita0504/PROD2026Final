package com.fruits.repository.user_network

import com.fruits.debug.DebugMockData
import com.fruits.debug.MockStorage
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate
import com.fruits.domain.repository.UserNetworkRepository
import com.fruits.network.user.schema.*
import com.fruits.network.user.service.UserService
import com.fruits.repository.util.mapResult
import com.fruits.repository.user_network.mapper.TokensMapper.toDomain
import com.fruits.repository.user_network.mapper.UserMapper.toDomain
import com.fruits.repository.util.mockOr

class UserNetworkRepositoryImpl(
    private val service: UserService,
    private val mockStorage: MockStorage,
    private val mockDataService: DebugMockData
) : UserNetworkRepository {

    override suspend fun login(email: String, password: String, androidPushToken: String?): Result<Tokens> =
        mockOr(mockStorage, { mockDataService.tokensMock }) {
            service.login(UserLoginSchema(email, password, androidPushToken)).mapResult { it.toDomain() }
        }

    override suspend fun getProfile(accessToken: String): Result<User> =
        mockOr(mockStorage, { mockDataService.userMock }) {
            service.getProfile(accessToken).mapResult { it.toDomain() }
        }

    override suspend fun refreshToken(refreshToken: String): Result<Tokens> =
        mockOr(mockStorage, { mockDataService.tokensMock }) {
            service.refreshToken(refreshToken).mapResult { it.toDomain() }
        }

    override suspend fun patchProfile(
        updateData: UserProfileUpdate,
        accessToken: String
    ): Result<User> =
        mockOr(mockStorage, { mockDataService.userMock }) {
            service.patchProfile(
                UserProfileUpdateScheme(updateData.description, updateData.photoFilesKeys),
                accessToken
            ).mapResult { it.toDomain() }
        }
}
