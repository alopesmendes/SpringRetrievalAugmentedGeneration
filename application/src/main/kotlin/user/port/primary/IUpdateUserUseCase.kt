package com.ailtontech.user.port.primary

import com.ailtontech.user.dto.UpdateUserCommand
import com.ailtontech.user.dto.UserResult

/**
 * Primary port for updating a User
 *
 */
fun interface IUpdateUserUseCase {
    /**
     * Updates the user with the data provided from [UpdateUserCommand]
     *
     * @param command The command containing the update data
     * @return Result containing [UserResult] the updated user
     */
    operator fun invoke(command: UpdateUserCommand): Result<UserResult>
}
