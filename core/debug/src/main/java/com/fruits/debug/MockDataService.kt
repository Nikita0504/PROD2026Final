package com.fruits.debug

import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate

class MockDataService {

    var userMock: User = User(
        id = "test",
        firstName = "Test",
        secondName = "User",
        email = "test@test.com",
        avatarFileKey = "test",
        readyToGive = true
    )

    var tokensMock: Tokens = Tokens(
        accessToken = "test_access_token",
        refreshToken = "test_refresh_token"
    )

    var userProfileUpdateMock: UserProfileUpdate = UserProfileUpdate(
        description = "Test description",
        photoFilesKeys = emptyList()
    )

    var uploadingDataMock: UploadingData = UploadingData(
        url = "https://mock-upload.test/image",
        key = "mock_file_key",
    )

    fun updateUser(block: User.() -> User) {
        userMock = userMock.block()
    }

    fun updateTokens(block: Tokens.() -> Tokens) {
        tokensMock = tokensMock.block()
    }

    fun updateUserProfileUpdate(block: UserProfileUpdate.() -> UserProfileUpdate) {
        userProfileUpdateMock = userProfileUpdateMock.block()
    }


    fun uploadingDataMockUpdate(block: UploadingData.() -> UploadingData) {
        uploadingDataMock = uploadingDataMock.block()
    }
}
