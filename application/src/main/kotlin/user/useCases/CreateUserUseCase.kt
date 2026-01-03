package com.ailtontech.user.useCases

import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.entity.User
import com.ailtontech.user.exception.UserAlreadyExistsException
import com.ailtontech.user.mapper.UserMapper.toResult
import com.ailtontech.user.mapper.UserMapper.toUserException
import com.ailtontech.user.port.primary.ICreateUserUseCase
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.services.PasswordService
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name

class CreateUserUseCase(
    private val userRepository: IUserRepository,
    private val passwordService: PasswordService,
    private val passwordHasherService: IPasswordHasherService,
) : ICreateUserUseCase {
    override fun invoke(command: CreateUserCommand): Result<UserResult> = runCatching {
        val email = Email.from(command.email)
        val exists = userRepository.existsByEmail(email)

        if (exists) {
            throw UserAlreadyExistsException(email)
        }

        passwordService.isValid(command.rawPassword)

        val passwordHash = passwordHasherService.hashPassword(command.rawPassword)
        val user =
            User.create(
                firstName = Name.from(command.firstName),
                lastName = Name.from(command.lastName),
                email = email,
                age = Age.from(command.age),
                password = passwordHash,
            )
        val saveUser = userRepository.save(user)

        saveUser.toResult()
    }.recoverCatching { exception -> throw exception.toUserException() }
}
