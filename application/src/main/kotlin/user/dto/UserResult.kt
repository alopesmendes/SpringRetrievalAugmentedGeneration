package com.ailtontech.user.dto

import java.time.Instant

/**
 * Result representing the User data
 *
 * @property id The User identifier
 * @property email The User email address
 * @property age The User age
 * @property firstName The User first name
 * @property lastName The User last name
 * @property createdAt Timestamp when the user was created
 * @property updatedAt Timestamp when the user was last updated
 */
data class UserResult(
    val id: String,
    val email: String,
    val age: Int,
    val firstName: String,
    val lastName: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
