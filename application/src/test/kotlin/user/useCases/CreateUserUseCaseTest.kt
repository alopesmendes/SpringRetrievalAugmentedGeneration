package com.ailtontech.user.useCases

import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.exception.InvalidUserDataException
import com.ailtontech.user.exception.UserAlreadyExistsException
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.services.PasswordService
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.PasswordHash
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("CreateUserUseCaseTest")
class CreateUserUseCaseTest {
    private lateinit var createUserUseCase: CreateUserUseCase
    private lateinit var userRepository: IUserRepository
    private lateinit var passwordService: PasswordService
    private lateinit var passwordHasherService: IPasswordHasherService

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService()
        userRepository = mockk()
        passwordHasherService = mockk()

        createUserUseCase =
            CreateUserUseCase(
                userRepository = userRepository,
                passwordService = passwordService,
                passwordHasherService = passwordHasherService,
            )
    }

    @Test
    fun `given valid command when using 'createUserUseCase()' then user is saved and returned`() {
        // Given:
        val email = Email.from("test@example.com")
        val command =
            CreateUserCommand(
                email = email.value,
                age = 25,
                rawPassword = "SecureP@ss123",
                firstName = "jane",
                lastName = "doe",
            )
        val hashedPassword = PasswordHash.from("hashedPassword")

        // When:
        every { userRepository.existsByEmail(email) } returns false
        every { passwordHasherService.hashPassword(any()) } returns hashedPassword
        every { userRepository.save(any()) } answers { firstArg() }
        val result = createUserUseCase(command)

        // Then:
        assertTrue(result.isSuccess)
        result.onSuccess { userResult ->
            assertTrue(userResult.id.isNotBlank())
            assertEquals(userResult.email, "test@example.com")
            assertEquals(userResult.age, 25)
            assertEquals(userResult.firstName, "Jane")
            assertEquals(userResult.lastName, "Doe")
        }
        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 1) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 1) { userRepository.save(any()) }
        confirmVerified(userRepository, passwordHasherService)
    }

    @Test
    fun `given an existing email when using 'createUserUseCase()' then throws 'UserAlreadyExistsException'`() {
        // Given:
        val email = Email.from("test@example.com")
        val command =
            CreateUserCommand(
                email = email.value,
                age = 25,
                rawPassword = "SecureP@ss123",
                firstName = "jane",
                lastName = "doe",
            )

        // When:
        every { userRepository.existsByEmail(email) } returns true
        val result = createUserUseCase(command)

        // Then:
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is UserAlreadyExistsException)
            assertEquals(exception.message, "The user already exists at this address $email")
        }
        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 0) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `given invalid password when using 'createUserUseCase()' then throws 'InvalidUserDataException'`() {
        // Given:
        val email = Email.from("test@example.com")
        val command =
            CreateUserCommand(
                email = email.value,
                age = 25,
                rawPassword = "",
                firstName = "jane",
                lastName = "doe",
            )

        // When:
        every { userRepository.existsByEmail(email) } returns false
        val result = createUserUseCase(command)

        // Then:
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is InvalidUserDataException)
        }
        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 0) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
