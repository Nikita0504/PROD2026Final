package com.fruits.debug

import com.fruits.domain.model.user.User

class MockDataService {
    var userMock: User = User(
        id = "sdsd",
        firstName = "dsd",
        secondName = "sdsd",
        email = "dsd",
        avatarFileKey = "dsd",
        readyToGive = true
    )

    fun updateUser(block: User.() -> User) { userMock = userMock.block() }

}