package com.ailtontech.user.adapter.primary.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a new user
 *
 * @property email The email address of the new user
 * @property age The age of the new user (between 13 and 120)
 * @property password The password of the new user (not the hashed password)
 * @property firstName The first name of the new user
 * @property lastName The last name of the new user
 */
@Schema(description = "Request payload for creating a new user")
data class CreateUserRequestDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Schema(description = "User's email address", example = "john.doe@example.com")
    @field:JsonProperty("email")
    val email: String,
    @field:NotNull(message = "Age is required")
    @field:Min(value = 13, message = "Age must be at least 13")
    @field:Max(value = 120, message = "Age must be at most 150")
    @field:Schema(description = "User's age", example = "25")
    @field:JsonProperty("age")
    val age: Int?,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @field:Schema(
        description = "User's password",
        example = "SecureP@ss123",
        accessMode = Schema.AccessMode.WRITE_ONLY,
    )
    @field:JsonProperty("password")
    val password: String,
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 128, message = "First name cannot exceed 128 characters")
    @field:Schema(description = "User's first name", example = "Jane")
    @field:JsonProperty("first_name")
    val firstName: String,
    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 128, message = "Last name cannot exceed 128 characters")
    @field:Schema(description = "User's last name", example = "Doe")
    @field:JsonProperty("last_name")
    val lastName: String,
)
