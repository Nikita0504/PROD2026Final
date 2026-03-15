package com.fruits.repository.user_network.mapper

import com.fruits.domain.model.user.User
import com.fruits.network.user.schema.UserReadSchema
import com.fruits.repository.user_network.mapper.UserMapper.toDomain
import com.fruits.repository.user_network.mapper.UserMapper.toSchema
import org.junit.Test
import kotlin.test.assertEquals

class UserMapperTest {

    @Test
    fun `UserReadSchema toDomain maps all fields correctly`() {
        // Given
        val schema = UserReadSchema(
            id = "user_123",
            firstName = "John",
            secondName = "Doe",
            email = "john@example.com",
            avatarFileKey = "avatar_key",
            readyToGive = true,
            description = "Test description",
            photoFileKeys = listOf("photo1", "photo2")
        )

        // When
        val user = schema.toDomain()

        // Then
        assertEquals("user_123", user.id)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.secondName)
        assertEquals("john@example.com", user.email)
        assertEquals("avatar_key", user.avatarFileKey)
        assertEquals(true, user.readyToGive)
        assertEquals("Test description", user.description)
        assertEquals(listOf("photo1", "photo2"), user.photoFileKeys)
    }

    @Test
    fun `UserReadSchema toDomain with null values maps correctly`() {
        // Given
        val schema = UserReadSchema(
            id = "user_456",
            firstName = "Jane",
            secondName = "Smith",
            email = "jane@example.com",
            avatarFileKey = null,
            readyToGive = false,
            description = null,
            photoFileKeys = emptyList()
        )

        // When
        val user = schema.toDomain()

        // Then
        assertEquals("user_456", user.id)
        assertEquals("Jane", user.firstName)
        assertEquals("Smith", user.secondName)
        assertEquals("jane@example.com", user.email)
        assertEquals(null, user.avatarFileKey)
        assertEquals(false, user.readyToGive)
        assertEquals(null, user.description)
        assertEquals(emptyList(), user.photoFileKeys)
    }

    @Test
    fun `User toSchema maps all fields correctly`() {
        // Given
        val user = com.fruits.domain.model.user.User(
            id = "user_789",
            firstName = "Bob",
            secondName = "Johnson",
            email = "bob@example.com",
            avatarFileKey = "bob_avatar",
            readyToGive = true,
            description = "Bob's description",
            photoFileKeys = listOf("bob_photo1")
        )

        // When
        val schema = user.toSchema()

        // Then
        assertEquals("Bob", schema.firstName)
        assertEquals("Johnson", schema.secondName)
        assertEquals("bob@example.com", schema.email)
        assertEquals("", schema.password) // password is hardcoded in mapper
        assertEquals(true, schema.readyToGive)
    }
}
