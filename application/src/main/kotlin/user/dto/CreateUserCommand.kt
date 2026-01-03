package com.ailtontech.user.dto

/**
 * Command for creating a new User
 *
 * @property email The User email address
 * @property age The User age
 * @property rawPassword The User raw password (will be hashed)
 * @property firstName The User first name
 * @property lastName The User last name
 */
data class CreateUserCommand(
    val email: String,
    val age: Int,
    val rawPassword: String,
    val firstName: String,
    val lastName: String,
)
