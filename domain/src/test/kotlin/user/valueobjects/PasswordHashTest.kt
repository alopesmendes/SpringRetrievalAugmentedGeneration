package com.ailtontech.user.valueobjects

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@DisplayName("PasswordHash")
class PasswordHashTest {
    @Nested
    @DisplayName("Create valid password hash")
    inner class PasswordHashValidation {
        @Test
        fun `should create valid password hash`() {
            val passwordHash = PasswordHash("password-hash")

            assertEquals("password-hash", passwordHash.value)
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
        fun `should reject blank values`(passwordHashValue: String) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    PasswordHash(passwordHashValue)
                }

            assertEquals(exception.message, "Password must not be blank")
        }
    }

    @Nested
    @DisplayName("Factory methods usage")
    inner class FactoryMethods {
        @Test
        fun `should create PasswordHash from valid string using 'from()'`() {
            val passwordHash = PasswordHash.from("password-hash")

            assertEquals("password-hash", passwordHash.value)
        }

        @Test
        fun `should reject PasswordHash from empty string using 'from()'`() {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    PasswordHash.from("")
                }

            assertEquals(exception.message, "Password must not be blank")
        }
    }

    @Nested
    @DisplayName("Password hash override methods")
    inner class PasswordHashOverride {
        @Test
        fun `should be equal when values are the same`() {
            val passwordHash1 = PasswordHash("password-hash")
            val passwordHash2 = PasswordHash("password-hash")

            assertEquals(passwordHash1.value, "password-hash")
            assertEquals(passwordHash1, passwordHash2)
        }

        @Test
        fun `should be not equal when values are different`() {
            val passwordHash1 = PasswordHash("password-hash")
            val passwordHash2 = PasswordHash("hash-password")

            assertNotEquals(passwordHash1, passwordHash2)
        }

        @Test
        fun `should have override 'toString()' to display a static string`() {
            val passwordHash = PasswordHash("password-hash")

            assertEquals(passwordHash.toString(), "PasswordHash(***)")
        }
    }
}
