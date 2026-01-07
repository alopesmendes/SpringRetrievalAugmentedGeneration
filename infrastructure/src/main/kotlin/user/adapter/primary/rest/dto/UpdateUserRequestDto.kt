package com.ailtontech.user.adapter.primary.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

/**
 * Request DTO for updating an existing user
 *
 * @property email The updated email address of the user
 * @property age The updated age of the user (between 13 and 120)
 * @property password The updated password of the user (not hashed password)
 * @property firstName The updated firstname of the user
 * @property lastName The updated lastname of the user
 */
@Schema(description = "Request payload for updating existing user")
data class UpdateUserRequestDto(
    @field:Email(message = "Invalid email format")
    @field:Schema(description = "User's email address", example = "john.doe@example.com")
    val email: String? = null,
    @field:Min(value = 13, message = "Age must be at least 13")
    @field:Max(value = 120, message = "Age must be at most 120")
    @field:Schema(description = "New age", example = "30", nullable = true)
    val age: Int? = null,
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @field:Schema(
        description = "User's password",
        example = "SecureP@ss123",
        accessMode = Schema.AccessMode.WRITE_ONLY,
    )
    @field:JsonProperty("password")
    val password: String? = null,
    @field:Size(max = 128, message = "First name cannot exceed 128 characters")
    @field:Schema(description = "User's first name", example = "Jane")
    @field:JsonProperty("first_name")
    val firstName: String? = null,
    @field:Size(max = 128, message = "Last name cannot exceed 128 characters")
    @field:Schema(description = "User's last name", example = "Doe")
    @field:JsonProperty("last_name")
    val lastName: String? = null,
)
