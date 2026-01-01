package com.ailtontech.user.valueobjects

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@DisplayName("NameTest")
class NameTest {
    @Nested
    @DisplayName("Create valid Name")
    inner class NameValidation {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "John Doe",
                "Le-Compte",
                "allo",
                "a",
            ],
        )
        fun `should create name with valid name`(nameValue: String) {
            val name = Name(nameValue)

            assertEquals(name.value, nameValue)
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "123",
                "John23",
                "Aa***",
                "Allo oui$",
            ],
        )
        fun `should reject name that are invalid`(nameValue: String) {
            val exception = assertFailsWith<IllegalArgumentException> { Name(nameValue) }

            assertEquals(exception.message, "Name format is invalid")
        }

        @Test
        fun `should reject name that exceed max length`() {
            val exception = assertFailsWith<IllegalArgumentException> { Name("a".repeat(300)) }

            assertEquals(exception.message, "Name is too long")
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
        fun `should reject name that are blank`(nameValue: String) {
            val exception = assertFailsWith<IllegalArgumentException> { Name(nameValue) }

            assertEquals(exception.message, "Name must not be blank")
        }
    }

    @Nested
    @DisplayName("Name Factory Methods")
    inner class FactoryMethods {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "John Doe",
                "Le-Compte       ",
            ],
        )
        fun `should create Name from valid value using 'from()'`(nameValue: String) {
            val name = Name.from(nameValue)

            assertEquals(name.value, nameValue.trim())
        }

        @Test
        fun `should reject Name creation from invalid value using 'from()'`() {
            val exception = assertFailsWith<IllegalArgumentException> { Name.from("") }
            assertEquals(exception.message, "Name must not be blank")
        }
    }

    @Nested
    @DisplayName("Name Override Methods")
    inner class NameOverride {
        @Test
        fun `should have equal name when given same value`() {
            val name1 = Name.from("John Doe")
            val name2 = Name.from("John Doe     ")

            assertEquals(name1, name2)
        }

        @Test
        fun `should have different name when given different value`() {
            val name1 = Name.from("John Doe")
            val name2 = Name.from("Le-Compte")

            assertNotEquals(name1, name2)
        }

        @Test
        fun `should have override 'toString()' to only display the name value`() {
            val name = Name.from("John Doe")

            assertEquals(name.toString(), "John Doe")
        }
    }

    @Nested
    @DisplayName("Name Methods and fields")
    inner class NameMethodsAndFields {
        @Test
        fun `should capitalize the name when calling 'capitalized' field`() {
            val name1 = Name.from("hugo")
            val name2 = Name.from("hugo boss")
            val name3 = Name.from("hugo      boSs")
            val name4 = Name.from("hugo\tboss")

            assertEquals(name1.capitalized, "Hugo")
            assertEquals(name2.capitalized, "Hugo Boss")
            assertEquals(name3.capitalized, "Hugo BoSs")
            assertEquals(name4.capitalized, "Hugo Boss")
        }

        @Test
        fun `should not capitalize name that is already capitalized`() {
            val name1 = Name.from("Hugo")
            val name2 = Name.from("Hugo Boss")

            assertEquals(name1.capitalized, "Hugo")
            assertEquals(name2.capitalized, "Hugo Boss")
        }
    }
}
