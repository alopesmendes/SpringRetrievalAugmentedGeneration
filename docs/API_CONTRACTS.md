# API Contracts

## Overview

This documentation is for the **Infrastructure Layer** (`adapter/primary/rest`). REST controllers serve as **inbound adapters** in our hexagonal architecture, translating HTTP requests into application commands and domain results back to HTTP responses.

---

## REST Conventions

### HTTP Methods

| Method | Purpose              | Success Code | Example                   |
|--------|----------------------|--------------|---------------------------|
| GET    | Retrieve resource(s) | 200          | `GET /api/users/{id}`     |
| POST   | Create resource      | 201          | `POST /api/users`         |
| PUT    | Full update          | 200          | `PUT /api/users/{id}`     |
| PATCH  | Partial update       | 200          | `PATCH /api/users/{id}`   |
| DELETE | Remove resource      | 204          | `DELETE /api/users/{id}`  |

### Request Data Types

| Type            | Annotation      | Use Case                      | Example                 |
|-----------------|-----------------|-------------------------------|-------------------------|
| Path Parameter  | `@PathVariable` | Resource identification       | `/{id}` → `id: String`  |
| Query Parameter | `@RequestParam` | Filtering, pagination         | `?page=0` → `page: Int` |
| Request Body    | `@RequestBody`  | JSON payload (POST/PUT/PATCH) | `CreateUserRequest`     |
| Multipart File  | `@RequestPart`  | File uploads                  | `file: MultipartFile`   |

### Status Codes

| Code | Meaning               | When to Use                                         |
|------|-----------------------|-----------------------------------------------------|
| 200  | OK                    | Successful GET, PUT, PATCH                          |
| 201  | Created               | Successful POST                                     |
| 204  | No Content            | Successful DELETE                                   |
| 400  | Bad Request           | Validation error                                    |
| 401  | Unauthorized          | Missing/invalid authentication                      |
| 403  | Forbidden             | Insufficient permissions                            |
| 404  | Not Found             | Resource doesn't exist                              |
| 409  | Conflict              | Duplicate resource                                  |
| 413  | Payload Too Large     | File exceeds limit                                  |
| 429  | Too Many Requests     | Rate limit exceeded (include `Retry-After` header)  |
| 500  | Internal Server Error | Unexpected error                                    |
| 503  | Service Unavailable   | Maintenance/overload (include `Retry-After` header) |

---

## OpenAPI Annotations

| Annotation                                | Level     | Purpose                                 |
|-------------------------------------------|-----------|-----------------------------------------|
| `@Tag(name, description)`                 | Class     | Group endpoints in Swagger UI           |
| `@Operation(summary, description)`        | Method    | Document endpoint                       |
| `@ApiResponse(responseCode, description)` | Method    | Document response                       |
| `@Parameter(description, example)`        | Parameter | Document path/query param               |
| `@Schema(description, example)`           | Field     | Document DTO field                      |
| `@Schema(accessMode = READ_ONLY)`         | Field     | Response-only field (`id`, `createdAt`) |
| `@Schema(accessMode = WRITE_ONLY)`        | Field     | Request-only field (`password`)         |

---

## Controller Annotations

### Class Level

| Annotation                         | Purpose                              |
|------------------------------------|--------------------------------------|
| `@RestController`                  | REST controller with `@ResponseBody` |
| `@RequestMapping("/api/v1/users")` | Base path                            |
| `@Tag(name = "Users")`             | OpenAPI grouping                     |

### Method Level

| Annotation                            | Purpose                 |
|---------------------------------------|-------------------------|
| `@GetMapping`, `@PostMapping`, etc.   | HTTP method mapping     |
| `@ResponseStatus(HttpStatus.CREATED)` | Override default status |
| `@Operation(summary = "...")`         | OpenAPI documentation   |

### Parameter Level

| Annotation      | Purpose                 |
|-----------------|-------------------------|
| `@PathVariable` | Extract from URL path   |
| `@RequestParam` | Extract query parameter |
| `@RequestBody`  | Deserialize JSON body   |
| `@RequestPart`  | Multipart form part     |
| `@Valid`        | Trigger validation      |

### Validation Annotations (on DTOs)

| Annotation                           | Purpose                |
|--------------------------------------|------------------------|
| `@NotNull`, `@NotBlank`, `@NotEmpty` | Required fields        |
| `@Size(min, max)`                    | String/collection size |
| `@Min`, `@Max`                       | Numeric bounds         |
| `@Email`, `@Pattern(regexp)`         | Format validation      |

---

## Request/Response DTOs

```kotlin
data class CreateUserRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    @field:Schema(description = "User's full name", example = "John Doe")
    val name: String,

    @field:NotBlank
    @field:Email
    @field:Schema(description = "Email address", example = "john@example.com")
    val email: String,

    @field:NotBlank
    @field:Size(min = 8)
    @field:Schema(description = "Password", accessMode = Schema.AccessMode.WRITE_ONLY)
    val password: String
)

data class UserResponse(
    @field:Schema(description = "User ID", accessMode = Schema.AccessMode.READ_ONLY)
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

data class ValidationErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldError>
)

data class FieldError(val field: String, val message: String)
```

---

## Global Exception Handler

Using `@RestControllerAdvice`, exceptions are handled centrally. Controllers simply throw domain exceptions—no try-catch needed.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(Instant.now(), 404, "Not Found", ex.message ?: "", getPath(request)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { FieldError(it.field, it.defaultMessage ?: "") }
        return ResponseEntity.badRequest()
            .body(ValidationErrorResponse(Instant.now(), 400, "Bad Request", "Validation failed", getPath(request), errors))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUpload(ex: MaxUploadSizeExceededException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ErrorResponse(Instant.now(), 413, "Payload Too Large", "File too large", getPath(request)))
    }

    private fun getPath(request: WebRequest) = (request as ServletWebRequest).request.requestURI
}
```

---

## Full Controller Example

```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management")
class UserController(
    private val createUserUseCase: ICreateUserUseCase,
    private val getUserUseCase: IGetUserUseCase,
    private val deleteUserUseCase: IDeleteUserUseCase,
    private val uploadImageUseCase: IUploadProfileImageUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Created"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "409", description = "Email already exists")
    )
    fun create(@Valid @RequestBody request: CreateUserRequest): UserResponse {
        return createUserUseCase(request.toCommand()).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Found"),
        ApiResponse(responseCode = "404", description = "Not found")
    )
    fun getById(@PathVariable id: String): UserResponse {
        return getUserUseCase(GetUserCommand(id)).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it }
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    fun delete(@PathVariable id: String) {
        deleteUserUseCase(DeleteUserCommand(id)).onFailure { throw it }
    }

    @PostMapping("/{id}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload profile image")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Uploaded"),
        ApiResponse(responseCode = "404", description = "User not found"),
        ApiResponse(responseCode = "413", description = "File too large")
    )
    fun uploadProfileImage(
        @PathVariable id: String,
        @RequestPart("file") file: MultipartFile
    ): ImageResponse {
        return uploadImageUseCase(
            UploadProfileImageCommand(
                userId = id,
                filename = file.originalFilename ?: "profile.jpg",
                contentType = file.contentType ?: "image/jpeg",
                bytes = file.bytes
            )
        ).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it }
        )
    }
}
```

---

## Versioning

### URL Path Versioning (Recommended)

```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserControllerV1

@RestController
@RequestMapping("/api/v2/users")
class UserControllerV2
```

### Header Versioning (Alternative)

```kotlin
@GetMapping(headers = ["X-API-Version=1"])
fun getUserV1(): UserResponseV1

@GetMapping(headers = ["X-API-Version=2"])
fun getUserV2(): UserResponseV2
```
