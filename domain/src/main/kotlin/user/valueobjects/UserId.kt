package com.ailtontech.user.valueobjects

import com.ailtontech.annotation.ValueObject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unique identifier for a User.
 *
 * @property value The string representation of the user identifier
 * @throws IllegalArgumentException if the value is blank
 */
@JvmInline
@ValueObject
value class UserId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "UserId must not be blank" }
    }

    override fun toString(): String = value

    companion object {
        /**
         * Creates a new UserId with a randomly generated UUID.
         *
         * @return A new UserId instance
         */
        @OptIn(ExperimentalUuidApi::class)
        fun generate(): UserId = UserId(Uuid.random().toString())

        /**
         * Creates a UserId from an existing string value.
         *
         * @param value The string value to create the UserId from
         * @return A new UserId instance
         * @throws IllegalArgumentException if the value is blank
         */
        fun from(value: String): UserId = UserId(value)
    }
}
