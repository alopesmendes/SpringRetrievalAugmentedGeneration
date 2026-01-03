package com.ailtontech.user.port.primary

import com.ailtontech.user.dto.GetUserCommand
import com.ailtontech.user.dto.UserResult

/**
 * Primary port for getting an existing User
 *
 */
fun interface IGetUserUseCase {
    /**
     * Gets the user with the data provided from [GetUserCommand]
     *
     * @param command The command containing the data to retrieve a user
     * @return Result containing [UserResult]
     */
    operator fun invoke(command: GetUserCommand): Result<UserResult>
}
