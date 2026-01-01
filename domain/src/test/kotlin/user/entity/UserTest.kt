package com.ailtontech.user.entity

import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.ParameterDeclarations
import java.time.Instant
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

data class UserMock(
    val id: String,
    val password: String,
    val age: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

private class UserFailureArguments : ArgumentsProvider {
    override fun provideArguments(
        parameters: ParameterDeclarations?,
        context: ExtensionContext?,
    ): Stream<out Arguments> {
        val defaultUser =
            UserMock(
                id = "user-123",
                password = "azejo",
                age = 25,
                email = "test@example.com",
                lastName = "boss",
                firstName = "hugo",
            )

        return Stream
            .of(
                // Invalid UserId
                defaultUser.copy(id = "  "),
                // Invalid password
                defaultUser.copy(password = " "),
                // Invalid age
                defaultUser.copy(age = 2),
                // Invalid email
                defaultUser.copy(email = "124"),
                // Invalid first name
                defaultUser.copy(lastName = "1"),
                // Invalid last name
                defaultUser.copy(lastName = " "),
            ).map(Arguments::of)
    }
}

private class UserSuccessArguments : ArgumentsProvider {
    override fun provideArguments(
        parameters: ParameterDeclarations?,
        context: ExtensionContext?,
    ): Stream<out Arguments> {
        val defaultUser =
            UserMock(
                id = "user-123",
                password = "passwordValid",
                age = 25,
                email = "test@example.com",
                lastName = "boss",
                firstName = "hugo",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        return Stream.of(defaultUser).map(Arguments::of)
    }
}

@DisplayName("UserTest")
class UserTest {
    @Nested
    @DisplayName("User valid creation")
    inner class UserValidation {
        @ParameterizedTest
        @ArgumentsSource(UserSuccessArguments::class)
        fun `should create a new user given valid data`(source: UserMock) {
            val user =
                User(
                    id = UserId.from(source.id),
                    password = PasswordHash.from(source.password),
                    age = Age.from(source.age),
                    firstName = Name.from(source.firstName),
                    lastName = Name.from(source.lastName),
                    email = Email.from(source.email),
                    createdAt = source.createdAt,
                    updatedAt = source.updatedAt,
                )

            assertEquals(user.id.value, source.id)
            assertEquals(user.password.value, source.password)
            assertEquals(user.age.value, source.age)
            assertEquals(user.email.value, source.email)
            assertEquals(user.lastName.value, source.lastName)
            assertEquals(user.firstName.value, source.firstName)
            assertEquals(user.createdAt, source.createdAt)
            assertEquals(user.updatedAt, source.updatedAt)
        }

        @ParameterizedTest
        @ArgumentsSource(UserFailureArguments::class)
        fun `should reject user with invalid data`(source: UserMock) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    User(
                        id = UserId.from(source.id),
                        password = PasswordHash.from(source.password),
                        email = Email.from(source.email),
                        age = Age.from(source.age),
                        firstName = Name.from(source.firstName),
                        lastName = Name.from(source.lastName),
                        createdAt = source.createdAt,
                        updatedAt = source.updatedAt,
                    )
                }

            assertTrue(!exception.message.isNullOrBlank())
        }
    }

    @Nested
    @DisplayName("User Factory Methods")
    inner class FactoryMethods {
        @ParameterizedTest
        @ArgumentsSource(UserSuccessArguments::class)
        fun `should create a new user given valid data using 'create()'`(source: UserMock) {
            val user =
                User.create(
                    firstName = Name.from(source.firstName),
                    lastName = Name.from(source.lastName),
                    password = PasswordHash.from(source.password),
                    age = Age.from(source.age),
                    email = Email.from(source.email),
                )

            assertEquals(user.password.value, source.password)
            assertEquals(user.age.value, source.age)
            assertEquals(user.email.value, source.email)
            assertEquals(user.lastName.value, source.lastName)
            assertEquals(user.firstName.value, source.firstName)

            // Since should generate new UserId
            assertNotEquals(user.id.value, source.id)
            // New Dates
            assertNotEquals(user.createdAt, source.createdAt)
            assertNotEquals(user.updatedAt, source.updatedAt)
        }

        @ParameterizedTest
        @ArgumentsSource(UserFailureArguments::class)
        fun `should reject user with invalid data using 'create()'`(source: UserMock) {
            // Skip the first test where the user id is invalid
            if (source.id.isBlank()) {
                return
            }

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    User.create(
                        firstName = Name.from(source.firstName),
                        lastName = Name.from(source.lastName),
                        password = PasswordHash.from(source.password),
                        age = Age.from(source.age),
                        email = Email.from(source.email),
                    )
                }

            assertTrue(!exception.message.isNullOrBlank())
        }

        @ParameterizedTest
        @ArgumentsSource(UserSuccessArguments::class)
        fun `should restore a user with valid data using 'from()'`(source: UserMock) {
            val user =
                User.from(
                    id = UserId.from(source.id),
                    password = PasswordHash.from(source.password),
                    age = Age.from(source.age),
                    firstName = Name.from(source.firstName),
                    lastName = Name.from(source.lastName),
                    email = Email.from(source.email),
                    createdAt = source.createdAt,
                    updatedAt = source.updatedAt,
                )

            assertEquals(user.id.value, source.id)
            assertEquals(user.password.value, source.password)
            assertEquals(user.age.value, source.age)
            assertEquals(user.email.value, source.email)
            assertEquals(user.lastName.value, source.lastName)
            assertEquals(user.firstName.value, source.firstName)
            assertEquals(user.createdAt, source.createdAt)
            assertEquals(user.updatedAt, source.updatedAt)
        }

        @ParameterizedTest
        @ArgumentsSource(UserFailureArguments::class)
        fun `should reject user with invalid data using 'from()'`(source: UserMock) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    User.from(
                        id = UserId.from(source.id),
                        password = PasswordHash.from(source.password),
                        age = Age.from(source.age),
                        firstName = Name.from(source.firstName),
                        lastName = Name.from(source.lastName),
                        email = Email.from(source.email),
                        createdAt = source.createdAt,
                        updatedAt = source.updatedAt,
                    )
                }

            assertTrue(!exception.message.isNullOrBlank())
        }
    }

    @Nested
    @DisplayName("User methods and fields")
    inner class UserMethodsAndFields {
        @Test
        fun `should update user with valid data`() {
            val user =
                User.create(
                    firstName = Name.from("Hugo"),
                    lastName = Name.from("Boss"),
                    password = PasswordHash.from("passwordvalid"),
                    age = Age.from(25),
                    email = Email.from("test@example.com"),
                )

            val updatedUser =
                user.update(
                    firstName = Name.from("Steve"),
                    lastName = Name.from("Lopes"),
                    age = Age.from(50),
                    email = Email.from("abc@gmail.fr"),
                )

            assertNotEquals(user.firstName, updatedUser.firstName)
            assertNotEquals(user.lastName, updatedUser.lastName)
            assertNotEquals(user.age, updatedUser.age)
            assertNotEquals(user.email, updatedUser.email)
            assertNotEquals(user.updatedAt, updatedUser.updatedAt)

            assertEquals(user.id, updatedUser.id)
            assertEquals(user.createdAt, updatedUser.createdAt)
            assertEquals(user.password, updatedUser.password)
        }

        @Test
        fun `should reject update with invalid data using 'update()'`() {
            val user =
                User.create(
                    firstName = Name.from("Hugo"),
                    lastName = Name.from("Boss"),
                    password = PasswordHash.from("passwordvalid"),
                    age = Age.from(25),
                    email = Email.from("test@example.com"),
                )

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    user.update(
                        firstName = Name.from(" "),
                        lastName = Name.from(""),
                        password = PasswordHash.from(""),
                        age = Age.from(2),
                        email = Email.from(""),
                    )
                }

            assertTrue(!exception.message.isNullOrBlank())
        }

        @ParameterizedTest
        @ArgumentsSource(UserSuccessArguments::class)
        fun `should return the user fullname when calling 'fullName' field`(source: UserMock) {
            val firstName = Name.from(source.firstName)
            val lastName = Name.from(source.lastName)

            val user =
                User.create(
                    firstName = firstName,
                    lastName = lastName,
                    password = PasswordHash.from(source.password),
                    age = Age.from(source.age),
                    email = Email.from(source.email),
                )

            assertEquals(user.fullName, "$firstName $lastName")
        }

        @ParameterizedTest
        @ArgumentsSource(UserSuccessArguments::class)
        fun `should capitalize the user name when calling 'fullNameCapitalized' field`(source: UserMock) {
            val firstName = Name.from(source.firstName)
            val lastName = Name.from(source.lastName)

            val user =
                User.create(
                    firstName = firstName,
                    lastName = lastName,
                    password = PasswordHash.from(source.password),
                    age = Age.from(source.age),
                    email = Email.from(source.email),
                )

            assertEquals(user.fullNameCapitalized, "${firstName.capitalized} ${lastName.capitalized}")
        }
    }
}
