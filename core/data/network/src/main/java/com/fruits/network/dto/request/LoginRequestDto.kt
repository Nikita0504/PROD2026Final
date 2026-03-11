package com.fruits.network.dto.request

import com.fruits.domain.model.LoginRequest
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
) {
    fun LoginRequestDto.toDomain(): LoginRequest {
        return LoginRequest(
            username = this.username,
            password = this.password
        )
    }
}
