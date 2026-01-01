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

@DisplayName("Email")
class EmailTest {
    @Nested
    @DisplayName("Create valid emails")
    inner class EmailValidation {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "xyz@example.com",
                "very.common@example.fr",
                "disposable.style.email.with+symbol@example.com",
                "other.email-with-hyphen@example.com",
                "x@example.com",
                "example@s.com",
            ],
        )
        fun `should create email with valid formats`(emailValue: String) {
            val email = Email(emailValue)

            assertEquals(email.value, emailValue)
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "plainaddress",
                "@no-local-part.com",
                "no-at-sign.com",
                "no-tld@example",
                "spaces in@example.com",
                "test@example.toolong",
            ],
        )
        fun `should reject emails with invalid formats`(emailValue: String) {
            val exception = assertFailsWith<IllegalArgumentException> { Email(emailValue) }

            assertEquals(exception.message, "Email format is invalid")
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "",
                "     ",
                "\t\t",
                "\n",
            ],
        )
        fun `should reject emails that are blank`(emailValue: String) {
            val exception = assertFailsWith<IllegalArgumentException> { Email(emailValue) }

            assertEquals(exception.message, "Email must not be blank")
        }

        @Test
        fun `should reject emails that exceed a certain length`() {
            val localPart = "a".repeat(64)
            val domain = "b".repeat(186) + ".com"
            val emailValue = "$localPart@$domain"

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    Email(emailValue)
                }

            assertTrue(emailValue.length > Email.MAX_LENGTH)
            assertEquals(exception.message, "Email cannot exceed ${Email.MAX_LENGTH} characters")
        }
    }

    @Nested
    @DisplayName("Factory methods usage")
    inner class FactoryMethods {
        @Test
        fun `should create emails using 'from()' when given emails with valid formats`() {
            val emailValue = "test@example.com"
            val email = Email.from(emailValue)

            assertEquals(email.value, emailValue)
        }

        @Test
        fun `should create emails using 'from()' that are trim and in lowercase`() {
            val emailValue = "tEsT@Example.com     "
            val email = Email.from(emailValue)

            assertEquals(email.value, emailValue.trim().lowercase())
        }

        @Test
        fun `should not create emails using 'from()' when given emails with invalid formats`() {
            val emailValue = "test"
            val exception = assertFailsWith<IllegalArgumentException> { Email.from(emailValue) }

            assertEquals(exception.message, "Email format is invalid")
        }
    }

    @Nested
    @DisplayName("Email Override Methods")
    inner class EmailOverride {
        @Test
        fun `should be same email when given the same value`() {
            val email1 = Email.from("test@example.com")
            val email2 = Email.from("tEsT@Example.Com     ")

            assertEquals(email1.value, "test@example.com")
            assertEquals(email1, email2)
        }

        @Test
        fun `should be different email when given the different value`() {
            val email1 = Email.from("abc@example.com")
            val email2 = Email.from("xyz@example.com")

            assertNotEquals(email1, email2)
        }

        @Test
        fun `should have override 'toString()' to only display value`() {
            val emailValue = "abc@example.com"
            val email = Email.from(emailValue)

            assertEquals(email.toString(), emailValue)
        }
    }
}
