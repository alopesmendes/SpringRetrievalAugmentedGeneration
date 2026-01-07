package com.ailtontech.user.adapter.primary.rest

import com.ailtontech.error.rest.GlobalExceptionHandler
import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.dto.GetUserCommand
import com.ailtontech.user.dto.UpdateUserCommand
import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.exception.UserAlreadyExistsException
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.port.primary.ICreateUserUseCase
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import com.ailtontech.user.valueobjects.Email
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Integration tests for [UserController].
 *
 * Uses standalone MockMvc setup with mocked use cases to test
 * HTTP request/response handling and exception translation.
 */
@WebMvcTest(controllers = [UserController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [UserController::class, GlobalExceptionHandler::class])
class UserControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createUserUseCase: ICreateUserUseCase

    @MockkBean
    private lateinit var getUserUseCase: IGetUserUseCase

    @MockkBean
    private lateinit var updateUserUseCase: IUpdateUserUseCase

    companion object {
        private const val BASE_URL = "/api/v1/users"
        private const val USER_ID = "550e8400-e29b-41d4-a716-446655440000"
        private const val USER_EMAIL = "john.doe@example.com"
        private const val USER_FIRSTNAME = "John"
        private const val USER_LASTNAME = "Doe"
        private const val USER_PASSWORD = "SecurePass123!"
        private const val USER_AGE = 30
    }

    @Nested
    inner class CreateUser {
        @Test
        fun postUsersEndpointReturns201WithValidRequest() {
            // Arrange
            val userResult = createUserResult()
            every { createUserUseCase(any<CreateUserCommand>()) } returns Result.success(userResult)

            val requestBody =
                """
                {
                    "email": "$USER_EMAIL",
                    "first_name": "$USER_FIRSTNAME",
                    "last_name": "$USER_LASTNAME",
                    "password": "$USER_PASSWORD",
                    "age": $USER_AGE
                }
                """.trimIndent()

            // Act & Assert
            mockMvc
                .perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andDo(print())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.first_name").value(USER_FIRSTNAME))
                .andExpect(jsonPath("$.last_name").value(USER_LASTNAME))
                .andExpect(jsonPath("$.age").value(USER_AGE))

            verify(exactly = 1) { createUserUseCase(any<CreateUserCommand>()) }
        }

        @Test
        fun postUsersEndpointReturns400WithInvalidEmail() {
            val requestBody =
                """
                {
                    "email": "invalid-email",
                    "first_name": "$USER_FIRSTNAME",
                    "last_name": "$USER_LASTNAME",
                    "password": "$USER_PASSWORD",
                    "age": $USER_AGE
                }
                """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun postUsersEndpointReturns400WithBlankName() {
            val requestBody =
                """
                {
                    "email": "$USER_EMAIL",
                    "first_name": "",
                    "last_name": "",
                    "password": "$USER_PASSWORD",
                    "age": $USER_AGE
                }
                """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun postUsersEndpointReturns400WithNegativeAge() {
            val requestBody =
                """
                {
                    "email": "$USER_EMAIL",
                    "first_name": "$USER_FIRSTNAME",
                    "last_name": "$USER_LASTNAME",
                    "password": "$USER_PASSWORD",
                    "age": -1
                }
                """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun postUsersEndpointReturns409WhenUserAlreadyExists() {
            val email = Email.from(USER_EMAIL)
            every { createUserUseCase(any<CreateUserCommand>()) } returns
                Result.failure(UserAlreadyExistsException(email))

            val requestBody =
                """
                {
                    "email": "$USER_EMAIL",
                    "first_name": "$USER_FIRSTNAME",
                    "last_name": "$USER_LASTNAME",
                    "password": "$USER_PASSWORD",
                    "age": $USER_AGE
                }
                """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isConflict)
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))

            verify(exactly = 1) { createUserUseCase(any<CreateUserCommand>()) }
        }
    }

    @Nested
    inner class GetUser {
        @Test
        fun getUserByIdReturns200WhenUserExists() {
            val userResult = createUserResult()
            every { getUserUseCase(GetUserCommand(USER_ID)) } returns Result.success(userResult)

            mockMvc
                .perform(
                    get("$BASE_URL/$USER_ID")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.first_name").value(USER_FIRSTNAME))
                .andExpect(jsonPath("$.last_name").value(USER_LASTNAME))
                .andExpect(jsonPath("$.age").value(USER_AGE))

            verify(exactly = 1) { getUserUseCase(GetUserCommand(USER_ID)) }
        }

        @Test
        fun getUserByIdReturns404WhenUserNotFound() {
            every { getUserUseCase(GetUserCommand(USER_ID)) } returns
                Result.failure(UserNotFoundException("User with id $USER_ID not found"))

            mockMvc
                .perform(
                    get("$BASE_URL/$USER_ID")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))

            verify(exactly = 1) { getUserUseCase(GetUserCommand(USER_ID)) }
        }
    }

    @Nested
    inner class UpdateUser {
        @Test
        fun putUserByIdReturns200WithValidRequest() {
            val updatedAge = 35
            val userResult = createUserResult(age = updatedAge)
            every { updateUserUseCase(any<UpdateUserCommand>()) } returns Result.success(userResult)

            val requestBody =
                """
                {
                    "first_name": "$USER_FIRSTNAME",
                    "age": $updatedAge
                }
                """.trimIndent()

            mockMvc
                .perform(
                    put("$BASE_URL/$USER_ID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.age").value(updatedAge))

            verify(exactly = 1) { updateUserUseCase(any<UpdateUserCommand>()) }
        }

        @Test
        fun putUserByIdReturns400WithInvalidAge() {
            val requestBody =
                """
                {
                    "first_name": "$USER_FIRSTNAME",
                    "age": -5
                }
                """.trimIndent()

            mockMvc
                .perform(
                    put("$BASE_URL/$USER_ID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun putUserByIdReturns404WhenUserNotFound() {
            every { updateUserUseCase(any<UpdateUserCommand>()) } returns
                Result.failure(UserNotFoundException("User with id $USER_ID not found"))

            val requestBody =
                """
                {
                }
                """.trimIndent()

            mockMvc
                .perform(
                    put("$BASE_URL/$USER_ID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))

            verify(exactly = 1) { updateUserUseCase(any<UpdateUserCommand>()) }
        }
    }

    private fun createUserResult(
        id: String = USER_ID,
        email: String = USER_EMAIL,
        firstName: String = USER_FIRSTNAME,
        lastName: String = USER_LASTNAME,
        age: Int = USER_AGE,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): UserResult = UserResult(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        age = age,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
