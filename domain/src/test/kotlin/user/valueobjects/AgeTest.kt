package com.ailtontech.user.valueobjects

import com.ailtontech.user.valueobjects.Age.Companion.MAX_AGE
import com.ailtontech.user.valueobjects.Age.Companion.MIN_AGE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@DisplayName("AgeTest")
class AgeTest {
    @Nested
    @DisplayName("Create valid Age")
    inner class AgeValidation {
        @Test
        fun `should create age with valid value`() {
            val age = Age(20)

            assertEquals(age.value, 20)
        }

        @ParameterizedTest
        @ValueSource(
            ints = [
                2,
                200,
            ],
        )
        fun `should not create age with value outside age bounds`(ageValue: Int) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    Age(ageValue)
                }

            assertEquals(exception.message, "$ageValue is not between $MIN_AGE and $MAX_AGE")
        }
    }

    @Nested
    @DisplayName("Factory methods usage")
    inner class FactoryMethodsUsage {
        @Test
        fun `should create Age from valid value using 'from()'`() {
            val age = Age.from(20)

            assertEquals(age.value, 20)
        }

        @Test
        fun `should not create Age with value outside the range using 'from()'`() {
            val valueAge = 2
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    Age.from(valueAge)
                }

            assertEquals(exception.message, "$valueAge is not between $MIN_AGE and $MAX_AGE")
        }
    }

    @Nested
    @DisplayName("Age Override Methods")
    inner class AgeOverride {
        @Test
        fun `should have equal age when given same value`() {
            val age1 = Age(20)
            val age2 = Age(20)

            assertEquals(age1.value, 20)
            assertEquals(age1, age2)
        }

        @Test
        fun `should have different age when given different value`() {
            val age1 = Age(20)
            val age2 = Age(25)

            assertNotEquals(age1, age2)
        }

        @Test
        fun `should have override 'toString()' to only display the age value`() {
            val age = Age(20)

            assertEquals(age.toString(), age.value.toString())
        }
    }
}
