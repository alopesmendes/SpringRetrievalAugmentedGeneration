package com.ailtontech.user.entity

import com.ailtontech.annotation.DomainEntity
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import java.time.Instant

/**
 * The entity that represents a User
 *
 * @property id The identifier of a User
 * @property firstName The first name of a User
 * @property lastName The last name of a User
 * @property email The email address of a User
 * @property age The age of a User (between 13 and 120)
 * @property password The hash password of a User
 * @property createdAt The creation date of a User
 * @property updatedAt The update date of a User
 * @throws IllegalArgumentException if user data is invalid
 */
@DomainEntity
data class User(
    val id: UserId,
    val firstName: Name,
    val lastName: Name,
    val email: Email,
    val age: Age,
    val password: PasswordHash,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /**
     * The fullname of a user
     */
    val fullName: String
        get() = "$firstName $lastName"

    /**
     * The full capitalized name of a user
     */
    val fullNameCapitalized: String
        get() = "${firstName.capitalized} ${lastName.capitalized}"

    /**
     * Update the user data, will also have a new updatedAt date
     *
     * @param firstName The first name of a User
     * @param lastName The last name of a User
     * @param email The email address of a User
     * @param age The age of a User (between 13 and 120)
     * @param password The hash password of a User
     * @return a copy of updated user
     * @throws IllegalArgumentException if user data is invalid
     */
    fun update(
        firstName: Name? = null,
        lastName: Name? = null,
        email: Email? = null,
        age: Age? = null,
        password: PasswordHash? = null,
    ): User = copy(
        firstName = firstName ?: this.firstName,
        lastName = lastName ?: this.lastName,
        email = email ?: this.email,
        age = age ?: this.age,
        password = password ?: this.password,
        updatedAt = Instant.now(),
    )

    companion object {
        /**
         * Creates a new instance of a User with a unique [UserId]
         *
         * @param firstName The first name of a User
         * @param lastName The last name of a User
         * @param email The email address of a User
         * @param age The age of a User (between 13 and 120)
         * @param password The hash password of a User
         * @return a new user instance
         * @throws IllegalArgumentException if user data is invalid
         */
        fun create(
            firstName: Name,
            lastName: Name,
            email: Email,
            age: Age,
            password: PasswordHash,
        ): User = User(
            id = UserId.generate(),
            firstName = firstName,
            lastName = lastName,
            email = email,
            age = age,
            password = password,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        /**
         * Creates a User with existing data
         *
         * @param id The identifier of a User
         * @param firstName The first name of a User
         * @param lastName The last name of a User
         * @param email The email address of a User
         * @param age The age of a User (between 13 and 120)
         * @param password The hash password of a User
         * @param createdAt The creation date of a User
         * @param updatedAt The update date of a User
         * @return creates a new user from existing data
         * @throws IllegalArgumentException if user data is invalid
         */
        fun from(
            id: UserId,
            firstName: Name,
            lastName: Name,
            email: Email,
            age: Age,
            password: PasswordHash,
            createdAt: Instant,
            updatedAt: Instant,
        ): User = User(
            id = id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            age = age,
            password = password,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
