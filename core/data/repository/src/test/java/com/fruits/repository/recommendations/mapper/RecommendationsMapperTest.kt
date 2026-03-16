package com.fruits.repository.recommendations.mapper

import com.fruits.network.recommendations.schema.RecommendationsSchema
import com.fruits.repository.recommendations.mapper.RecommendationsMapper.toDomain
import org.junit.Test
import kotlin.test.assertEquals

class RecommendationsMapperTest {

    @Test
    fun `RecommendationsSchema toDomain maps all fields correctly`() {
        // Given
        val schema = RecommendationsSchema(
            userId = "user_123",
            firstName = "John",
            secondName = "Doe",
            age = 25,
            city = "Moscow",
            photoFileKeys = listOf("photo1", "photo2"),
            description = "Test description",
            explanation = listOf("Explanation 1", "Explanation 2")
        )

        // When
        val recommendations = schema.toDomain()

        // Then
        assertEquals("user_123", recommendations.userId)
        assertEquals("John", recommendations.firstName)
        assertEquals("Doe", recommendations.secondName)
        assertEquals(25, recommendations.age)
        assertEquals("Moscow", recommendations.city)
        assertEquals(listOf("photo1", "photo2"), recommendations.photoFileKeys)
        assertEquals("Test description", recommendations.description)
        assertEquals(listOf("Explanation 1", "Explanation 2"), recommendations.explanation)
        assertEquals(emptyList(), recommendations.tags)
    }

    @Test
    fun `RecommendationsSchema toDomain maps tags correctly`() {
        val schema = RecommendationsSchema(
            userId = "user_1",
            firstName = "A",
            secondName = "B",
            age = 30,
            city = "City",
            photoFileKeys = emptyList(),
            description = "Desc",
            explanation = emptyList(),
            tags = listOf("Яблоки", "Фрукты"),
        )
        val recommendations = schema.toDomain()
        assertEquals(listOf("Яблоки", "Фрукты"), recommendations.tags)
    }

    @Test
    fun `RecommendationsSchema toDomain with empty lists maps correctly`() {
        // Given
        val schema = RecommendationsSchema(
            userId = "user_456",
            firstName = "Jane",
            secondName = "Smith",
            age = 30,
            city = "Saint Petersburg",
            photoFileKeys = emptyList(),
            description = "Jane's description",
            explanation = emptyList()
        )

        // When
        val recommendations = schema.toDomain()

        // Then
        assertEquals("user_456", recommendations.userId)
        assertEquals("Jane", recommendations.firstName)
        assertEquals("Smith", recommendations.secondName)
        assertEquals(30, recommendations.age)
        assertEquals("Saint Petersburg", recommendations.city)
        assertEquals(emptyList(), recommendations.photoFileKeys)
        assertEquals("Jane's description", recommendations.description)
        assertEquals(emptyList(), recommendations.explanation)
    }

    @Test
    fun `List of RecommendationsSchema toDomain maps all items`() {
        // Given
        val schemas = listOf(
            RecommendationsSchema(
                userId = "user_1",
                firstName = "First",
                secondName = "User",
                age = 20,
                city = "City1",
                photoFileKeys = listOf("p1"),
                description = "Desc1",
                explanation = listOf("Exp1")
            ),
            RecommendationsSchema(
                userId = "user_2",
                firstName = "Second",
                secondName = "User",
                age = 25,
                city = "City2",
                photoFileKeys = listOf("p2", "p3"),
                description = "Desc2",
                explanation = listOf("Exp2")
            )
        )

        // When
        val recommendationsList = schemas.toDomain()

        // Then
        assertEquals(2, recommendationsList.size)
        assertEquals("user_1", recommendationsList[0].userId)
        assertEquals("user_2", recommendationsList[1].userId)
        assertEquals("First", recommendationsList[0].firstName)
        assertEquals("Second", recommendationsList[1].firstName)
    }

    @Test
    fun `Empty list toDomain returns empty list`() {
        // Given
        val schemas = emptyList<RecommendationsSchema>()

        // When
        val recommendationsList = schemas.toDomain()

        // Then
        assertEquals(0, recommendationsList.size)
    }
}
