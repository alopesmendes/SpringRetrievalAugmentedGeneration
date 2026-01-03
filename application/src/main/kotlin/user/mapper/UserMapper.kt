package com.ailtontech.user.mapper

import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.entity.User
import com.ailtontech.user.exception.InvalidUserDataException
import com.ailtontech.user.exception.UserException
import com.ailtontech.user.exception.UserUnknownException

/**
 * The mapper object that will have the extension mapping function for User
 */
object UserMapper {
    /**
     * Extension method that will map a [User] into a [UserResult]
     *
     * @return The [UserResult] that represents a [User]
     */
    fun User.toResult(): UserResult = UserResult(
        id = id.value,
        email = email.value,
        age = age.value,
        firstName = firstName.capitalized,
        lastName = lastName.capitalized,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    /**
     * Extension method that will map a [Throwable] into a [UserUnknownException]
     *
     * @return The [UserUnknownException] that represents the exception
     */
    fun Throwable.toUserException(): UserException = when (this) {
        is UserException -> this
        is IllegalArgumentException -> InvalidUserDataException(this)
        else -> UserUnknownException(this)
    }
}
