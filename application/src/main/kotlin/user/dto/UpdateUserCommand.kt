package com.ailtontech.user.dto

/**
 * Command to update an existing User
 * All the fields are optional - only non-null fields will be updated
 *
 * @property id The User id to retrieve the user that needs to be updated
 * @property email The User email address
 * @property age The User age
 * @property rawPassword The User raw password (will be hashed)
 * @property firstName The User first name
 * @property lastName The User last name
 */
data class UpdateUserCommand(
    val id: String,
    val email: String? = null,
    val age: Int? = null,
    val rawPassword: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
)
