package com.fruits.network.dto.response

import com.fruits.domain.model.AuthToken
import com.fruits.domain.model.LoginRequest
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenDto(
     val accessToken: String,
     val refreshToken: String,
) {
     fun AuthTokenDto.toDomain(): AuthToken {
          return AuthToken (
               accessToken = this.accessToken,
               refreshToken = this.refreshToken
          )
     }
}
