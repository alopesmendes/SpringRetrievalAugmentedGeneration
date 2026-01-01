package com.ailtontech.user.valueobjects

import com.ailtontech.annotation.ValueObject

/**
 * The age of a User
 *
 * @property value the int representation of the age of the person
 * @throws IllegalArgumentException if the value is not between [MIN_AGE] and [MAX_AGE]
 */
@JvmInline
@ValueObject
value class Age(
    val value: Int,
) {
    init {
        require(value in MIN_AGE..MAX_AGE) { "$value is not between $MIN_AGE and $MAX_AGE" }
    }

    override fun toString(): String = value.toString()

    companion object {
        const val MIN_AGE = 13
        const val MAX_AGE = 120

        /**
         * Creates an Age from an existing int value
         *
         * @param value the int value to create the Age from
         * @return a new Age instance
         * @throws IllegalArgumentException if the value is blank
         */
        fun from(value: Int): Age = Age(value)
    }
}
