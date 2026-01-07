package com.ailtontech.error.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * The field validation error that occurred
 *
 * @property field the field that failed the validation
 * @property message the validation error message
 */
@Schema(description = "Field validation error")
data class FieldErrorDto(
    @field:Schema(description = "Field name that failed validation", example = "email")
    @field:JsonProperty("field")
    val field: String,
    @field:Schema(description = "Validation error message", example = "Invalid email format")
    @field:JsonProperty("message")
    val message: String,
)
