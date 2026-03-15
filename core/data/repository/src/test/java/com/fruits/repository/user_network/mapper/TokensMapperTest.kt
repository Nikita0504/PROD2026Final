package com.fruits.repository.user_network.mapper

import com.fruits.domain.model.user.Tokens
import com.fruits.network.user.schema.TokenReadSchema
import com.fruits.repository.user_network.mapper.TokensMapper.toDomain
import com.fruits.repository.user_network.mapper.TokensMapper.toRefreshSchema
import org.junit.Test
import kotlin.test.assertEquals

class TokensMapperTest {

    @Test
    fun `TokenReadSchema toDomain maps all fields correctly`() {
        // Given
        val schema = TokenReadSchema(
            accessToken = "access_token_123",
            refreshToken = "refresh_token_456"
        )

        // When
        val tokens = schema.toDomain()

        // Then
        assertEquals("access_token_123", tokens.accessToken)
        assertEquals("refresh_token_456", tokens.refreshToken)
    }

    @Test
    fun `TokenReadSchema toDomain with empty strings maps correctly`() {
        // Given
        val schema = TokenReadSchema(
            accessToken = "",
            refreshToken = ""
        )

        // When
        val tokens = schema.toDomain()

        // Then
        assertEquals("", tokens.accessToken)
        assertEquals("", tokens.refreshToken)
    }

    @Test
    fun `Tokens toRefreshSchema maps refreshToken correctly`() {
        // Given
        val tokens = Tokens(
            accessToken = "access_123",
            refreshToken = "refresh_456"
        )

        // When
        val schema = tokens.toRefreshSchema()

        // Then
        assertEquals("refresh_456", schema.refreshToken)
    }

    @Test
    fun `Tokens toRefreshSchema ignores accessToken`() {
        // Given
        val tokens = Tokens(
            accessToken = "access_123",
            refreshToken = "refresh_456"
        )

        // When
        val schema = tokens.toRefreshSchema()

        // Then
        assertEquals("refresh_456", schema.refreshToken)
        // accessToken is not included in RefreshTokenSchema
    }
}
