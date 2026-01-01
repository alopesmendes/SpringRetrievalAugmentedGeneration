package com.ailtontech.user.services

import com.ailtontech.annotation.DomainService

/**
 * Domain service for password-related business rules.
 * This service handles domain validation rules for passwords.
 */
@DomainService
class PasswordService {
    /**
     * Checks if the raw password is meets the domain requirements
     *
     * @param rawPassword plain text password to check
     * @return true if the password meets all the requirements
     * @throws IllegalArgumentException if password is invalid
     */
    fun isValid(rawPassword: String): Boolean {
        require(rawPassword.isNotEmpty()) { "rawPassword must not be empty" }
        require(!rawPassword.contains(Regex("\\s"))) { "rawPassword must not contain spaces" }
        require(
            rawPassword.length >= MIN_PASSWORD_LENGTH,
        ) { "rawPassword must be at least $MIN_PASSWORD_LENGTH characters" }
        require(
            rawPassword.length <= MAX_PASSWORD_LENGTH,
        ) { "rawPassword must be at most $MAX_PASSWORD_LENGTH characters" }
        require(rawPassword.any { it.isLowerCase() }) { "rawPassword must contain at least one lowercase letter" }
        require(rawPassword.any { it.isUpperCase() }) { "rawPassword must contain at least one uppercase letter" }
        require(rawPassword.any { it.isDigit() }) { "rawPassword must contain at least one digit" }
        require(
            rawPassword.any { it in SPECIAL_CHARACTERS },
        ) { "rawPassword must contain at least one special character" }

        return true
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
        const val SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
    }
}
