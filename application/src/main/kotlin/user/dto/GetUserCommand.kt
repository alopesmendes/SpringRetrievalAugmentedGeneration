package com.ailtontech.user.dto

/**
 * Command to get an existing User
 *
 * @property id The User id to retrieve the user
 */
data class GetUserCommand(
    val id: String,
)
