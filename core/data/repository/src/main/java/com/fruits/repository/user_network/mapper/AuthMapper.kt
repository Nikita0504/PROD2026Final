package com.fruits.repository.user_network.mapper

import com.fruits.domain.model.user.AuthResult
import com.fruits.network.user.schema.UserRegisterSchema
import com.fruits.repository.user_network.mapper.TokensMapper.toDomain
import com.fruits.repository.user_network.mapper.UserMapper.toDomain

object AuthMapper {

    fun UserRegisterSchema.toDomain(): AuthResult =
        AuthResult(
            user = user.toDomain(),
            tokens = tokens.toDomain()
        )
}