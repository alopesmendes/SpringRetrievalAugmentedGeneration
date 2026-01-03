package com.ailtontech.user.exception

import com.ailtontech.user.valueobjects.Email

/**
 * All user domain exceptions
 *
 * @property message
 * @property cause
 */
sealed class UserException(
    override val message: String,
    override val cause: Throwable?,
) : RuntimeException(message, cause)

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 *
 * @property email The duplicate [Email]
 */
data class UserAlreadyExistsException(
    val email: Email,
) : UserException(
        message = "The user already exists at this address $email",
        cause = IllegalArgumentException("The user already exists at this address $email"),
    )

/**
 * Exception thrown when a user is not found.
 *
 * @property id The id value that was not found
 */
data class UserNotFoundException(
    val id: String,
) : UserException(
        message = "Not found user with this $id",
        cause = IllegalArgumentException("Not found user with this $id"),
    )

/**
 * Exception thrown when user data validation fails
 *
 * @property cause The cause for the validation failure
 */
data class InvalidUserDataException(
    override val cause: Throwable,
) : UserException(
        message = cause.message ?: "",
        cause = cause,
    )

/**
 * Exception throw when catches an unknown error
 *
 * @property cause The cause for the unknown error
 */
data class UserUnknownException(
    override val cause: Throwable,
) : UserException(
        message = cause.message ?: "",
        cause = cause,
    )
