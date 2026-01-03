package com.ailtontech.user.useCases

import com.ailtontech.user.dto.GetUserCommand
import com.ailtontech.user.entity.User
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.secondary.IUserRepository
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
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetUserUseCaseTest {
    private lateinit var userRepository: IUserRepository
    private lateinit var getUserUseCase: IGetUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        getUserUseCase = GetUserUseCase(userRepository)
    }

    @Test
    fun `given existing id when using 'getUserUseCase' then returns the user data`() {
        // Given:
        val id = "user-id"
        val command = GetUserCommand(id)
        val user =
            User.from(
                id = UserId.from(id),
                firstName = Name.from("Jane"),
                lastName = Name.from("Doe"),
                email = Email.from("jane@doe.com"),
                age = Age.from(20),
                password = PasswordHash.from("password-hash"),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        // When:
        every { userRepository.findById(UserId.from(id)) } returns user
        val result = getUserUseCase(command)

        // Then:
        assertTrue(result.isSuccess)
        result.onSuccess { getUser ->
            assertEquals(getUser.id, user.id.value)
            assertEquals(getUser.firstName, user.firstName.value)
            assertEquals(getUser.lastName, user.lastName.value)
            assertEquals(getUser.email, user.email.value)
            assertEquals(getUser.age, user.age.value)
            assertEquals(getUser.createdAt, user.createdAt)
            assertEquals(getUser.updatedAt, user.updatedAt)
        }
        verify(exactly = 1) { userRepository.findById(any()) }
        confirmVerified(userRepository)
    }

    @Test
    fun `given unexisting id when using 'getUserUseCase' then returns 'UserNotFoundException'`() {
        // Given:
        val id = "user-id"
        val command = GetUserCommand(id)

        // When:
        every { userRepository.findById(UserId.from(id)) } returns null
        val result = getUserUseCase(command)

        // Then:
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is UserNotFoundException)
            assertEquals(exception.message, "Not found user with this $id")
        }
        verify(exactly = 1) { userRepository.findById(UserId.from(id)) }
        confirmVerified(userRepository)
    }
}
