package com.ailtontech.user.useCases

import com.ailtontech.user.dto.GetUserCommand
import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.mapper.UserMapper.toResult
import com.ailtontech.user.mapper.UserMapper.toUserException
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.valueobjects.UserId

class GetUserUseCase(
    private val userRepository: IUserRepository,
) : IGetUserUseCase {
    override fun invoke(command: GetUserCommand): Result<UserResult> = runCatching {
        val userId = UserId.from(command.id)
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(command.id)

        user.toResult()
    }.recoverCatching { exception -> throw exception.toUserException() }
}
