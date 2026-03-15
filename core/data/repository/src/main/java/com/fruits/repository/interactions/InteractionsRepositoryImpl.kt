package com.fruits.repository.interactions

import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.network.interactions.schema.UserActionCreateSchema
import com.fruits.network.interactions.schema.UserReportCreateSchema
import com.fruits.network.interactions.service.InteractionsService
import com.fruits.repository.util.mapResult

class InteractionsRepositoryImpl(
    private val service: InteractionsService,
) : InteractionsRepository {

    override suspend fun sendAction(
        accessToken: String,
        targetUserId: String,
        action: UserAction,
    ): Result<Unit> {
        val request = UserActionCreateSchema(
            targetUserId = targetUserId,
            action = action.name,
        )

        return service
            .sendAction(accessToken, request)
            .mapResult { Unit }
    }

    override suspend fun reportUser(
        accessToken: String,
        targetUserId: String,
        reason: ReportReason,
        comment: String?,
    ): Result<Unit> {
        val request = UserReportCreateSchema(
            targetUserId = targetUserId,
            reason = reason.name,
            comment = comment,
        )

        return service
            .reportUser(accessToken, request)
            .mapResult { Unit }
    }
}

