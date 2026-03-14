package com.fruits.debugPanel.extension

import com.fruits.debugPanel.UserMockEditState
import com.fruits.domain.model.user.User

fun User.toEditState() = UserMockEditState(firstName, secondName, email)
