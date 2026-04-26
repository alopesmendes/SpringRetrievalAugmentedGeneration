---
name: spring-rest-mvc
description: Spring MVC REST adapter patterns for this project. Covers @RestController structure, DTO design, @Valid validation, springdoc-openapi annotations, GlobalExceptionHandler with @RestControllerAdvice, Result<T> folding at HTTP boundary, and how error responses are shaped. Trigger when writing or reviewing controllers, DTOs, request/response mappers, exception handlers, or OpenAPI annotations.
---

# Spring REST MVC Skill

Inbound adapter layer. Translates HTTP ↔ application ports. Zero business logic here.

## Controller anatomy

```kotlin
@RestController
@RequestMapping("/api/v1/{feature}s")
@Tag(name = "Users", description = "...")
class UserController(
    private val createUser: ICreateUserUseCase,   // input port, not impl
    private val getUser: IGetUserUseCase,
) {
    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "...", description = "...")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", ...),
        ApiResponse(responseCode = "400", ...),
        ApiResponse(responseCode = "409", ...),
    ])
    fun createUser(@Valid @RequestBody request: CreateUserRequestDto): UserResponseDto =
        createUser(request.toCommand()).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it },          // GlobalExceptionHandler catches
        )
}
```

Key rules:
- Inject input port interfaces — never concrete use case classes.
- `throw it` on failure — `@RestControllerAdvice` maps to HTTP. Never return raw error from controller.
- `@Valid` + Jakarta Bean Validation on every inbound DTO. `MethodArgumentNotValidException` handled globally.
- `@ResponseStatus` preferred over returning `ResponseEntity` when status is fixed.

## DTO design

**Request DTO** — Jakarta validation annotations. Located in `adapter/primary/rest/dto/`.

```kotlin
data class CreateUserRequestDto(
    @field:NotBlank val email: String,
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    @field:NotBlank val password: String,
    @field:Min(0) @field:Max(150) val age: Int,
)
```

**Response DTO** — no validation. Data class with all fields.

```kotlin
data class UserResponseDto(val id: String, val email: String, val firstName: String, val lastName: String, val age: Int)
```

Naming: `{Entity}{Purpose}Dto` — `CreateUserRequestDto`, `UserResponseDto`, `UpdateUserRequestDto`.

## Mapper — extension functions

Located in `adapter/primary/rest/mapper/{Feature}RestMapper.kt`. Never inside use case or controller.

```kotlin
object UserRestMapper {
    fun CreateUserRequestDto.toCommand(): CreateUserCommand =
        CreateUserCommand(Email(email), Name(firstName, lastName), password, Age(age))

    fun UserResult.toResponse(): UserResponseDto =
        UserResponseDto(id, email, firstName, lastName, age)
}
```

Import via static: `import com.ailtontech.user.adapter.primary.rest.mapper.UserRestMapper.toCommand`.

## GlobalExceptionHandler

`error/rest/GlobalExceptionHandler.kt` — single `@RestControllerAdvice` for all features.

Mapped exceptions today:

| Exception                         | HTTP               |
|-----------------------------------|--------------------|
| `UserNotFoundException`           | 404                |
| `UserAlreadyExistsException`      | 409                |
| `InvalidUserDataException`        | 400                |
| `UserUnknownException`            | 400                |
| `MethodArgumentNotValidException` | 400 + field errors |
| `Exception` (fallback)            | 500                |

Adding a new feature's exceptions: add `@ExceptionHandler` method in `GlobalExceptionHandler`. Same file — one handler for all.

Error response shapes:

```kotlin
data class ErrorResponseDto(
    val timestamp: Instant, val status: Int, val error: String,
    val message: String?, val path: String,
)

data class ValidationErrorResponseDto(
    val timestamp: Instant, val status: Int, val error: String,
    val message: String, val path: String,
    val fieldErrors: List<FieldErrorDto>,
)

data class FieldErrorDto(val field: String, val message: String)
```

## OpenAPI / springdoc

Every controller method gets:
- `@Operation(summary, description)` — one-liners.
- `@ApiResponses` — document all non-200 codes.
- `@Parameter` on path variables.
- `@Tag` on the class.

Config lives in `config/OpenApiConfig.kt`. Access at `/swagger-ui.html` and `/v3/api-docs`.

## URL conventions

```
/api/v1/{feature-plural}           GET (list), POST (create)
/api/v1/{feature-plural}/{id}      GET, PUT, DELETE (by id)
```

Always `APPLICATION_JSON_VALUE` on `produces` + `consumes`. `jakarta.validation.Valid` not `javax`.

## What NOT to do

- No business logic in controller — only translate + delegate.
- No domain imports in DTO — use `String`, `Int`, primitives.
- No `ResponseEntity<>` return unless status varies per case; use `@ResponseStatus` instead.
- No `try/catch` in controller — let `GlobalExceptionHandler` handle.
