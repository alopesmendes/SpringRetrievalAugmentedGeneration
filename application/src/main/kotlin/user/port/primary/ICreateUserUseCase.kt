package com.ailtontech.user.port.primary

import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.dto.UserResult

/**
 * Primary port for creating a new User
 *
 */
fun interface ICreateUserUseCase {
    /**
     * Creates a new user with the data provided from [CreateUserCommand]
     *
     * @param command The command containing the creation data
     * @return Result containing [UserResult] the created user
     */
    operator fun invoke(command: CreateUserCommand): Result<UserResult>
}
