package com.ailtontech.user.adapter.primary.rest.mapper

import com.ailtontech.user.adapter.primary.rest.dto.CreateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.dto.UpdateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.dto.UserResponseDto
import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.dto.UpdateUserCommand
import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.exception.InvalidUserDataException

/**
 * The mapper object that have the extension functions for the dto's
 */
object UserRestMapper {
    /**
     * Extension method that will map [CreateUserRequestDto] into a [CreateUserCommand]
     *
     * @return The [CreateUserCommand] that represents the [CreateUserRequestDto]
     */
    fun CreateUserRequestDto.toCommand(): CreateUserCommand = CreateUserCommand(
        email = email,
        age = age ?: throw InvalidUserDataException(IllegalArgumentException("Age is required")),
        rawPassword = password,
        firstName = firstName,
        lastName = lastName,
    )

    fun UpdateUserRequestDto.toCommand(id: String): UpdateUserCommand = UpdateUserCommand(
        id = id,
        email = email,
        age = age,
        rawPassword = password,
        firstName = firstName,
        lastName = lastName,
    )

    /**
     * Extension method that will map [UserResult] into a [UserResponseDto]
     *
     * @return The [UserResponseDto] that represents the [UserResult]
     */
    fun UserResult.toResponse(): UserResponseDto = UserResponseDto(
        id = id,
        email = email,
        age = age,
        firstName = firstName,
        lastName = lastName,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
