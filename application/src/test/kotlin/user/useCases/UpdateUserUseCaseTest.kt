package com.ailtontech.user.useCases

import com.ailtontech.user.dto.UpdateUserCommand
import com.ailtontech.user.entity.User
import com.ailtontech.user.exception.InvalidUserDataException
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.services.PasswordService
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("UpdateUserUseCaseTest")
class UpdateUserUseCaseTest {
    private lateinit var userRepository: IUserRepository
    private lateinit var passwordService: PasswordService
    private lateinit var passwordHasherService: IPasswordHasherService
    private lateinit var updateUserUseCase: IUpdateUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordHasherService = mockk()
        passwordService = PasswordService()
        updateUserUseCase =
            UpdateUserUseCase(
                userRepository = userRepository,
                passwordService = passwordService,
                passwordHasherService = passwordHasherService,
            )
    }

    @Test
    fun `given valid command when using 'updateUserUseCase()' then user is updated and saved`() {
        // Given:
        val id = "user-id"
        val now = Instant.now()
        val command =
            UpdateUserCommand(
                id = id,
                email = "jane@doe.com",
                age = 30,
                rawPassword = "SecureP@ss567",
            )
        val user =
            User.from(
                id = UserId.from(id),
                age = Age.from(20),
                firstName = Name.from("Hugo"),
                lastName = Name.from("Boss"),
                email = Email.from("test@example.com"),
                password = PasswordHash.from("SecureP@ss123"),
                createdAt = now,
                updatedAt = now,
            )

        // When:
        every { userRepository.findById(UserId.from(command.id)) } returns user
        every { passwordHasherService.hashPassword(any()) } returns PasswordHash.from(command.rawPassword ?: "")
        every { userRepository.save(any()) } answers { firstArg() }

        val result = updateUserUseCase(command)

        // Then:
        assertTrue(result.isSuccess)
        result.onSuccess { updatedUser ->
            // Not updated values
            assertEquals(updatedUser.id, user.id.value)
            assertEquals(updatedUser.createdAt, user.createdAt)
            // Not updated values because did not give them to use case
            assertEquals(updatedUser.firstName, user.firstName.value)
            assertEquals(updatedUser.lastName, user.lastName.value)
            assertNotEquals(updatedUser.firstName, command.firstName)
            assertNotEquals(updatedUser.lastName, command.lastName)
            // Updated values
            assertEquals(updatedUser.age, command.age)
            assertEquals(updatedUser.email, command.email)
            // Updated every time
            assertNotEquals(updatedUser.updatedAt, user.updatedAt)
        }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 1) { userRepository.findById(any()) }
        confirmVerified(userRepository, passwordHasherService)
    }

    @Test
    fun `given unexisting id when using 'updateUserUseCase()' then throws 'UserNotFoundException'`() {
        // Given:
        val id = "user-id"
        val command =
            UpdateUserCommand(
                id = id,
                age = 27,
            )

        // When:
        every { userRepository.findById(UserId.from(command.id)) } returns null
        val result = updateUserUseCase(command)

        // Then:
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is UserNotFoundException)
            assertEquals(exception.message, "Not found user with this $id")
        }
        verify(exactly = 1) { userRepository.findById(any()) }
        verify(exactly = 0) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
        confirmVerified(userRepository, passwordHasherService)
    }

    @Test
    fun `given invalid data when using 'updateUserUseCase()' then throws 'InvalidUserDataException'`() {
        // Given:
        val id = "user-id"
        val now = Instant.now()
        val command =
            UpdateUserCommand(
                id = id,
                rawPassword = "",
            )
        val user =
            User.from(
                id = UserId.from(id),
                age = Age.from(20),
                firstName = Name.from("Hugo"),
                lastName = Name.from("Boss"),
                email = Email.from("test@example.com"),
                password = PasswordHash.from("SecureP@ss123"),
                createdAt = now,
                updatedAt = now,
            )

        // When:
        every { userRepository.findById(UserId.from(command.id)) } returns user
        val result = updateUserUseCase(command)

        // Then:
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is InvalidUserDataException)
        }
        verify(exactly = 1) { userRepository.findById(any()) }
        verify(exactly = 0) { passwordHasherService.hashPassword(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
        confirmVerified(userRepository, passwordHasherService)
    }
}
