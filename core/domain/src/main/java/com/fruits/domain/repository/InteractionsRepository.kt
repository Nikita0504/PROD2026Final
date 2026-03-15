package com.fruits.domain.repository

import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.model.interactions.UserAction

interface InteractionsRepository {

    suspend fun sendAction(
        accessToken: String,
        targetUserId: String,
        action: UserAction,
    ): Result<Unit>

    suspend fun reportUser(
        accessToken: String,
        targetUserId: String,
        reason: ReportReason,
        comment: String?,
    ): Result<Unit>


}

