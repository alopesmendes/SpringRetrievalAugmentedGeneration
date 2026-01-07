package com.ailtontech.user.config

import com.ailtontech.user.port.primary.ICreateUserUseCase
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.services.PasswordService
import com.ailtontech.user.useCases.CreateUserUseCase
import com.ailtontech.user.useCases.GetUserUseCase
import com.ailtontech.user.useCases.UpdateUserUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserBeanConfig {
    @Bean
    fun passwordService(): PasswordService = PasswordService()

    @Bean
    fun createUserUseCase(
        userRepository: IUserRepository,
        passwordHasher: IPasswordHasherService,
        passwordService: PasswordService,
    ): ICreateUserUseCase = CreateUserUseCase(
        userRepository = userRepository,
        passwordHasherService = passwordHasher,
        passwordService = passwordService,
    )

    @Bean
    fun getUserUseCase(userRepository: IUserRepository): IGetUserUseCase = GetUserUseCase(
        userRepository = userRepository,
    )

    @Bean
    fun updateUserUseCase(
        userRepository: IUserRepository,
        passwordHasher: IPasswordHasherService,
        passwordService: PasswordService,
    ): IUpdateUserUseCase = UpdateUserUseCase(
        userRepository = userRepository,
        passwordHasherService = passwordHasher,
        passwordService = passwordService,
    )
}
