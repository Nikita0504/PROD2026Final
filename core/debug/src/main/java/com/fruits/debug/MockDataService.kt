package com.fruits.debug

import com.fruits.domain.model.user.User

class MockDataService {
    var userMock: User = User(
        id = 2,
        firstName = "dsd",
        secondName = "sdsd",
        email = "dsd",
        avatarFileKey = "dsd"
    )

    fun updateUser(block: User.() -> User) { userMock = userMock.block() }

}