package com.fruits.repository.user_network.mapper

import com.fruits.domain.model.user.Tokens
import com.fruits.network.user.schema.RefreshTokenSchema
import com.fruits.network.user.schema.TokenReadSchema

object TokensMapper {

    fun TokenReadSchema.toDomain(): Tokens =
        Tokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )

    fun Tokens.toRefreshSchema(): RefreshTokenSchema =
        RefreshTokenSchema(refreshToken = refreshToken)
}