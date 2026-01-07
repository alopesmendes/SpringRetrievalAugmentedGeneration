package com.ailtontech.error.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Response DTO for validation error
 *
 * @property timestamp The date when the error occurred
 * @property status The HTTP status code
 * @property error The reason of the error
 * @property message The general error message
 * @property path The path of the request
 * @property fieldErrors The list of [FieldErrorDto]
 */
@Schema(description = "Validation error response")
data class ValidationErrorResponseDto(
    @field:Schema(description = "Time when the error occurred")
    @field:JsonProperty("timestamp")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val timestamp: Instant,
    @field:Schema(description = "HTTP status code", example = "400")
    @field:JsonProperty("status")
    val status: Int,
    @field:Schema(description = "HTTP status reason phrase", example = "Bad Request")
    @field:JsonProperty("error")
    val error: String,
    @field:Schema(description = "General error message", example = "Validation failed")
    @field:JsonProperty("message")
    val message: String,
    @field:Schema(description = "Request path")
    @field:JsonProperty("path")
    val path: String,
    @field:Schema(description = "List of field-specific validation errors")
    @field:JsonProperty("fields")
    val fieldErrors: List<FieldErrorDto>,
)
