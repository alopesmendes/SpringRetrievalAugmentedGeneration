package com.ailtontech.error.rest

import com.ailtontech.error.rest.dto.ErrorResponseDto
import com.ailtontech.error.rest.dto.FieldErrorDto
import com.ailtontech.error.rest.dto.ValidationErrorResponseDto
import com.ailtontech.user.exception.InvalidUserDataException
import com.ailtontech.user.exception.UserAlreadyExistsException
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.exception.UserUnknownException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        ex: UserNotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponseDto(
                timestamp = Instant.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.reasonPhrase,
                message = ex.message,
                path = getPath(request),
            ),
        )

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(
        ex: UserAlreadyExistsException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseDto(
                timestamp = Instant.now(),
                status = HttpStatus.CONFLICT.value(),
                error = HttpStatus.CONFLICT.reasonPhrase,
                message = ex.message,
                path = getPath(request),
            ),
        )

    @ExceptionHandler(InvalidUserDataException::class)
    fun handleInvalidArgument(
        ex: InvalidUserDataException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = ex.message,
                path = getPath(request),
            ),
        )

    @ExceptionHandler(UserUnknownException::class)
    fun handleUnknownException(
        ex: UserUnknownException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseDto(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = ex.message,
                path = getPath(request),
            ),
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ValidationErrorResponseDto> {
        val fieldErrors =
            ex.bindingResult.fieldErrors.map { fieldError ->
                FieldErrorDto(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponseDto(
                    timestamp = Instant.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "Validation failed",
                    path = getPath(request),
                    fieldErrors = fieldErrors,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponseDto(
                timestamp = Instant.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = ex.message ?: "An unexpected error occurred",
                path = getPath(request),
            ),
        )

    private fun getPath(request: WebRequest): String = (request as ServletWebRequest).request.requestURI
}
