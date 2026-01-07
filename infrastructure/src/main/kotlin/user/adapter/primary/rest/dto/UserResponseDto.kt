package com.ailtontech.user.adapter.primary.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Response DTO for getting a User info
 *
 * @property id The id of the user
 * @property email The email address of the user
 * @property age The age of the user
 * @property firstName The first name of the user
 * @property lastName The last name of the user
 * @property createdAt The date the user was created (immutable)
 * @property updatedAt The date the user was updated (mutable)
 */
@Schema(description = "User Info")
data class UserResponseDto(
    @field:Schema(description = "User's identifier", example = "b59525a6-2b49-463a-97f2-5a9711c240d7")
    @field:JsonProperty("id")
    val id: String,
    @field:Schema(description = "User's email", example = "jane@doe.com")
    @field:JsonProperty("email")
    val email: String,
    @field:Schema(description = "User's age", example = "20")
    @field:JsonProperty("age")
    val age: Int,
    @field:Schema(description = "User's first name", example = "Jane")
    @field:JsonProperty("first_name")
    val firstName: String,
    @field:Schema(description = "User's last name", example = "Doe")
    @field:JsonProperty("last_name")
    val lastName: String,
    @field:Schema(
        description = "Timestamp when the user was created",
        example = "2024-01-15T10:30:00Z",
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    @field:JsonProperty("created_at")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val createdAt: Instant,
    @field:Schema(
        description = "Timestamp when the user was updated",
        example = "2024-01-15T10:30:00Z",
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    @field:JsonProperty("updated_at")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val updatedAt: Instant,
)
