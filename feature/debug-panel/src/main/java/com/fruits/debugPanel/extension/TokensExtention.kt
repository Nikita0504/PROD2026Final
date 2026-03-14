package com.fruits.debugPanel.extension

import com.fruits.debugPanel.TokensMockEditState
import com.fruits.domain.model.user.Tokens

fun Tokens.toEditState() = TokensMockEditState(accessToken, refreshToken)
