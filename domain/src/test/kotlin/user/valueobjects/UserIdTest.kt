package com.ailtontech.user.valueobjects

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("UserId")
class UserIdTest {
    @Nested
    @DisplayName("Create valid user ids")
    inner class UserIdValidation {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "a",
                "user-123",
                "550e8400-e29b-41d4-a716-446655440000",
            ],
        )
        fun `should create user id with valid value`(userIdValue: String) {
            val userId = UserId(userIdValue)

            assertEquals(userId.value, userIdValue)
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "",
                "        ",
                "\t\t",
                "\n",
            ],
        )
        fun `should reject blank values`(userIdValue: String) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    UserId(userIdValue)
                }

            assertEquals(exception.message, "UserId must not be blank")
        }
    }

    @Nested
    @DisplayName("Factory methods usage")
    inner class FactoryMethods {
        @Test
        fun `should generate unique 'UserId'`() {
            val userId1 = UserId.generate()
            val userId2 = UserId.generate()

            assertNotEquals(userId1, userId2)
        }

        @Test
        fun `should generate UserId with valid UUID format`() {
            val userId = UserId.generate()
            val uuidRegex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

            assertTrue(uuidRegex.matches(userId.value))
        }

        @Test
        fun `should create UserId from valid string using 'from()'`() {
            val userId = UserId.from("user-456")

            assertEquals("user-456", userId.value)
        }

        @Test
        fun `should reject blank string in 'from()'`() {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    UserId.from("")
                }

            assertEquals(exception.message, "UserId must not be blank")
        }
    }

    @Nested
    @DisplayName("UserId Override Methods")
    inner class UserIdOverride {
        @Test
        fun `should be equal when values are the same`() {
            val userId1 = UserId.from("user-123")
            val userId2 = UserId.from("user-123")

            assertEquals(userId1.value, "user-123")
            assertEquals(userId1, userId2)
        }

        @Test
        fun `should be not equal when values are different`() {
            val userId1 = UserId.from("user-123")
            val userId2 = UserId.from("user-456")

            assertNotEquals(userId1, userId2)
        }

        @Test
        fun `should have override 'toString()' to only display value`() {
            val userId = UserId.from("user-789")

            assertEquals("user-789", userId.toString())
        }
    }
}
