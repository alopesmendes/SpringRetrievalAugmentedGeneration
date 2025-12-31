package com.ailtontech.user.valueobjects

import com.ailtontech.annotation.ValueObject

/**
 * The hash password that is encrypted
 *
 * @property value the string representation of the hash password
 * @throws IllegalArgumentException if the value is blank
 * @throws
 */
@JvmInline
@ValueObject
value class PasswordHash(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Password must not be blank" }
    }

    override fun toString(): String = "PasswordHash(***)"

    companion object {
        /**
         * Creates a PasswordHash from a hash string.
         *
         * @param hash The hashed password
         * @return A new PasswordHash instance
         */
        fun from(hash: String): PasswordHash = PasswordHash(hash)
    }
}
