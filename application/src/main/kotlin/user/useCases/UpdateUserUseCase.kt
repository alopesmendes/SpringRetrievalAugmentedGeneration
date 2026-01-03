package com.ailtontech.user.useCases

import com.ailtontech.user.dto.UpdateUserCommand
import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.mapper.UserMapper.toResult
import com.ailtontech.user.mapper.UserMapper.toUserException
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.services.PasswordService
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.UserId

class UpdateUserUseCase(
    private val userRepository: IUserRepository,
    private val passwordService: PasswordService,
    private val passwordHasherService: IPasswordHasherService,
) : IUpdateUserUseCase {
    override fun invoke(command: UpdateUserCommand): Result<UserResult> = runCatching {
        val user = userRepository.findById(UserId.from(command.id)) ?: throw UserNotFoundException(command.id)

        val passwordHash =
            if (command.rawPassword != null && passwordService.isValid(command.rawPassword)) {
                passwordHasherService.hashPassword(command.rawPassword)
            } else {
                null
            }

        val updatedUser =
            user.update(
                firstName = command.firstName?.let(Name::from),
                lastName = command.lastName?.let(Name::from),
                email = command.email?.let(Email::from),
                age = command.age?.let(Age::from),
                password = passwordHash,
            )

        val savedUser = userRepository.save(updatedUser)

        savedUser.toResult()
    }.recoverCatching { exception -> throw exception.toUserException() }
}
