package com.ailtontech.user.valueobjects

import com.ailtontech.annotation.ValueObject
import java.util.Locale.getDefault

/**
 * The name of a User
 *
 * @property value the string representation of the name of a user (can be firstname and lastname)
 * @throws IllegalArgumentException if the name is invalid
 */
@JvmInline
@ValueObject
value class Name(
    val value: String,
) {
    init {
        require(value.length <= MAX_LENGTH) { "Name is too long" }
        require(value.isNotBlank()) { "Name must not be blank" }
        require(value.matches(VALID_NAME_REGEX)) { "Name format is invalid" }
    }

    /**
     * Returns the capitalized name like: hugo boss becomes Hugo Boss
     */
    val capitalized: String
        get() =
            value
                .split(Regex("\\s+"))
                .joinToString(separator = " ", transform = Name::capitalizeName)

    override fun toString(): String = value

    companion object {
        private const val MAX_LENGTH = 128
        private val VALID_NAME_REGEX = Regex("^[\\p{L}\\s'-]+$")

        /**
         * Creates a Name from existing string value
         *
         * @param value the string value to create the Name from
         * @return a new Name instance
         * @throws IllegalArgumentException if the name is invalid
         */
        fun from(value: String): Name = Name(value.trim())

        /**
         * Title case the first character only if it's lowercase
         */
        private fun titlecaseIfLowercase(character: Char): CharSequence = if (character.isLowerCase()) {
            character.titlecase(getDefault())
        } else {
            character.toString()
        }

        private fun capitalizeName(value: String) = value.replaceFirstChar(Name::titlecaseIfLowercase)
    }
}
