package com.fruits.repository.interactions

import com.fruits.debug.MockDataService
import com.fruits.debug.MockStorage
import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.model.interactions.ReportReason
import com.fruits.domain.model.interactions.UserAction
import com.fruits.domain.repository.InteractionsRepository
import com.fruits.network.interactions.schema.UserActionCreateSchema
import com.fruits.network.interactions.schema.UserReportCreateSchema
import com.fruits.network.interactions.service.InteractionsService
import com.fruits.network.util.ApiResult
import com.fruits.repository.interactions.mapper.toDomain
import com.fruits.repository.util.mapResult
import com.fruits.repository.util.mockOr

class InteractionsRepositoryImpl(
    private val service: InteractionsService,
    private val mockStorage: MockStorage,
    private val mockDataService: MockDataService,
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

    override suspend fun getIncomingLikes(
        accessToken: String,
    ): Result<List<IncomingLike>> {
        return mockOr(mockStorage, { mockDataService.incomingLikes }) {
            val apiResult = service.getIncomingLikes(accessToken)
            if (apiResult is ApiResult.Error) {
                return@mockOr Result.failure(Exception("[${apiResult.code}] ${apiResult.message}"))
            }

            val schemas = (apiResult as ApiResult.Success).data

            ApiResult.Success(schemas).mapResult { list ->
                list.map { schema ->
                    schema.toDomain()
                }
            }
        }
    }
}

