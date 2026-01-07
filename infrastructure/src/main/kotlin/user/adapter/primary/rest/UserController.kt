package com.ailtontech.user.adapter.primary.rest

import com.ailtontech.error.rest.dto.ErrorResponseDto
import com.ailtontech.error.rest.dto.ValidationErrorResponseDto
import com.ailtontech.user.adapter.primary.rest.dto.CreateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.dto.UpdateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.dto.UserResponseDto
import com.ailtontech.user.adapter.primary.rest.mapper.UserRestMapper.toCommand
import com.ailtontech.user.adapter.primary.rest.mapper.UserRestMapper.toResponse
import com.ailtontech.user.dto.GetUserCommand
import com.ailtontech.user.port.primary.ICreateUserUseCase
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val createUserUseCase: ICreateUserUseCase,
    private val updateUserUseCase: IUpdateUserUseCase,
    private val getUserUseCase: IGetUserUseCase,
) {
    /**
     * Endpoint to create a new User (POST).
     *
     * @param request The request dto [CreateUserRequestDto] to create a user
     * @return The response dto [UserResponseDto] to get info of the created user
     */
    @PostMapping(
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided information")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = [Content(schema = Schema(implementation = UserResponseDto::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ValidationErrorResponseDto::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "User with email already exists",
                content = [Content(schema = Schema(implementation = ErrorResponseDto::class))],
            ),
        ],
    )
    fun createUser(
        @Valid
        @RequestBody
        request: CreateUserRequestDto,
    ): UserResponseDto = createUserUseCase(request.toCommand()).fold(
        onSuccess = { it.toResponse() },
        onFailure = { throw it },
    )

    /**
     * Endpoint to get an existing User (GET)
     *
     * @param id The identifier of the user
     * @return The response dto [UserResponseDto] to get user info
     */
    @GetMapping(
        path = ["/{id}"],
        produces = [APPLICATION_JSON_VALUE],
    )
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique identifier")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User found",
                content = [Content(schema = Schema(implementation = UserResponseDto::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponseDto::class))],
            ),
        ],
    )
    fun getUserById(
        @Parameter(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: String,
    ): UserResponseDto = getUserUseCase(GetUserCommand(id)).fold(
        onSuccess = { it.toResponse() },
        onFailure = { throw it },
    )

    @PutMapping(
        path = ["/{id}"],
        produces = [APPLICATION_JSON_VALUE],
        consumes = [APPLICATION_JSON_VALUE],
    )
    @Operation(
        summary = "Updates data of an existing user",
        description = "Updates a user by their unique identifier",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = [Content(schema = Schema(implementation = UserResponseDto::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ValidationErrorResponseDto::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponseDto::class))],
            ),
        ],
    )
    fun updateUserById(
        @Parameter(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable
        id: String,
        @Valid
        @RequestBody request: UpdateUserRequestDto,
    ): UserResponseDto = updateUserUseCase(request.toCommand(id)).fold(
        onSuccess = { it.toResponse() },
        onFailure = { throw it },
    )
}
