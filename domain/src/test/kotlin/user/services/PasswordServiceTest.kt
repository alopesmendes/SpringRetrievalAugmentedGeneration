package com.ailtontech.user.services

import com.ailtontech.user.services.PasswordService.Companion.MAX_PASSWORD_LENGTH
import com.ailtontech.user.services.PasswordService.Companion.MIN_PASSWORD_LENGTH
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class PasswordFailureArguments : ArgumentsProvider {
    override fun provideArguments(
        parameters: ParameterDeclarations?,
        context: ExtensionContext?,
    ): Stream<out Arguments> = Stream
        .of(
            "" to "rawPassword must not be empty",
            "aaa" to "rawPassword must be at least $MIN_PASSWORD_LENGTH characters",
            "aaa bbb" to "rawPassword must not contain spaces",
            "aaa".repeat(256) to "rawPassword must be at most $MAX_PASSWORD_LENGTH characters",
            "MOTDEPASSESECURISE" to "rawPassword must contain at least one lowercase letter",
            "motdepassesecurise" to "rawPassword must contain at least one uppercase letter",
            "MotDePasseSecurise" to "rawPassword must contain at least one digit",
            "MotDePasseSecurise123" to "rawPassword must contain at least one special character",
        ).map(Arguments::of)
}

@DisplayName("PasswordServiceTest")
class PasswordServiceTest {
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService()
    }

    @ParameterizedTest
    @ArgumentsSource(PasswordFailureArguments::class)
    fun `when the password given is invalid then method 'isValid' will throw an error`(
        passwordAndException: Pair<String, String>,
    ) {
        val (password, exceptionMessage) = passwordAndException
        val exception =
            assertFailsWith<IllegalArgumentException> {
                passwordService.isValid(password)
            }

        assertEquals(exception.message, exceptionMessage)
    }

    @Test
    fun `when the password given is valid then method 'isValid' will return true`() {
        val isValid = passwordService.isValid("MotDePasseSecurise123&")

        assertTrue(isValid)
    }
}
