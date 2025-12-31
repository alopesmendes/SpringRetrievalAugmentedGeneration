package com.ailtontech.user.valueobjects

import com.ailtontech.annotation.ValueObject

/**
 * An email with a valid format
 *
 * @property value The string representation of the Email
 * @throws IllegalArgumentException if the value format of the email is invalid
 */
@JvmInline
@ValueObject
value class Email(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Email must not be blank" }
        require(value.length <= MAX_LENGTH) { "Email cannot exceed $MAX_LENGTH characters" }
        require(value.matches(EMAIL_REGEX)) { "Email format is invalid" }
    }

    override fun toString() = value

    companion object {
        private val EMAIL_REGEX = Regex("""^[\w-.+]+@([\w-]+\.)+[\w-]{2,4}$""")
        const val MAX_LENGTH = 254

        /**
         * Creates an Email from an existing string value
         *
         * @param value The string value to create the Email from
         * @return A new Email instance
         * @throws IllegalArgumentException if the email is invalid
         */
        fun from(value: String): Email = Email(value.trim().lowercase())
    }
}
