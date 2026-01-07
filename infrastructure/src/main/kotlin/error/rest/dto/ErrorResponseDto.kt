package com.ailtontech.error.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Response DTO for error
 *
 * @property timestamp The date when the error occurred
 * @property status The HTTP status code
 * @property error The reason of the error
 * @property message The general error message
 * @property path The path of the request
 */
@Schema(description = "Standard error response")
data class ErrorResponseDto(
    @field:Schema(description = "Time when the error occurred", example = "2024-01-15T10:30:00Z")
    @field:JsonProperty("timestamp")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val timestamp: Instant,
    @field:Schema(description = "HTTP status code", example = "404")
    @field:JsonProperty("status")
    val status: Int,
    @field:Schema(description = "HTTP status reason phrase", example = "Not Found")
    @field:JsonProperty("error")
    val error: String,
    @field:Schema(description = "Error message", example = "User with id '123' not found")
    @field:JsonProperty("message")
    val message: String,
    @field:Schema(description = "Request path", example = "/api/v1/users/123")
    @field:JsonProperty("path")
    val path: String,
)
