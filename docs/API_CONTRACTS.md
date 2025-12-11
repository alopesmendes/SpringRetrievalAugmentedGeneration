# API Contracts

## REST Conventions

### HTTP Methods

| Method | Purpose | Success Code | Example |
|--------|---------|--------------|---------|
| GET | Retrieve resource(s) | 200 | `GET /api/images/{id}` |
| POST | Create resource | 201 | `POST /api/images` |
| PUT | Full update | 200 | `PUT /api/images/{id}` |
| PATCH | Partial update | 200 | `PATCH /api/images/{id}` |
| DELETE | Remove resource | 204 | `DELETE /api/images/{id}` |

### URL Patterns

```
/api/{resource}          → Collection (plural)
/api/{resource}/{id}     → Single resource
/api/{resource}/{id}/{sub-resource}  → Nested resource
```

### Status Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Internal Server Error | Unexpected error |

---

## Request/Response Structure

### Request DTOs

```kotlin
data class CreateExampleRequest(
    val name: String,
    val description: String?
)

data class UpdateExampleRequest(
    val name: String?,
    val description: String?
)
```

### Response DTOs

```kotlin
data class ExampleResponse(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
```

---

## Error Handling

### Error Response Format

```kotlin
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

data class FieldError(
    val field: String,
    val message: String
)
```

### Global Exception Handler

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(...))
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(...))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { 
            FieldError(it.field, it.defaultMessage ?: "Invalid value") 
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse(..., fieldErrors = fieldErrors))
    }
}
```

---

## Controller Annotations Cheat Sheet

### Class Level

| Annotation | Purpose |
|------------|---------|
| `@RestController` | Combines `@Controller` + `@ResponseBody` |
| `@RequestMapping("/api/examples")` | Base path for all endpoints |
| `@Tag(name = "Examples")` | OpenAPI grouping |

### Method Level

| Annotation | Purpose |
|------------|---------|
| `@GetMapping` | HTTP GET |
| `@PostMapping` | HTTP POST |
| `@PutMapping("/{id}")` | HTTP PUT with path variable |
| `@PatchMapping("/{id}")` | HTTP PATCH with path variable |
| `@DeleteMapping("/{id}")` | HTTP DELETE with path variable |
| `@ResponseStatus(HttpStatus.CREATED)` | Override default status code |

### Parameter Level

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@RequestBody` | Deserialize request body | `@RequestBody request: CreateRequest` |
| `@Valid` | Trigger validation | `@Valid @RequestBody request: CreateRequest` |
| `@PathVariable` | Extract path variable | `@PathVariable id: String` |
| `@RequestParam` | Query parameter | `@RequestParam(defaultValue = "0") page: Int` |
| `@RequestHeader` | Header value | `@RequestHeader("Authorization") token: String` |

### Validation Annotations (on DTOs)

| Annotation | Purpose |
|------------|---------|
| `@NotNull` | Field cannot be null |
| `@NotBlank` | String not null/empty/whitespace |
| `@NotEmpty` | Collection not null/empty |
| `@Size(min, max)` | String/collection size |
| `@Min(value)` / `@Max(value)` | Numeric bounds |
| `@Email` | Valid email format |
| `@Pattern(regexp)` | Regex match |

### Full Controller Example

```kotlin
@RestController
@RequestMapping("/api/examples")
@Tag(name = "Examples", description = "Example management endpoints")
class ExampleController(
    private val createUseCase: ICreateExampleUseCase,
    private val getUseCase: IGetExampleUseCase,
    private val deleteUseCase: IDeleteExampleUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new example")
    fun create(
        @Valid @RequestBody request: CreateExampleRequest
    ): ExampleResponse {
        return createUseCase(request.toCommand()).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get example by ID")
    fun getById(
        @PathVariable id: String
    ): ExampleResponse {
        return getUseCase(GetExampleCommand(id)).fold(
            onSuccess = { it.toResponse() },
            onFailure = { throw it }
        )
    }

    @GetMapping
    @Operation(summary = "Get all examples")
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PagedResponse<ExampleResponse> {
        // ...
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete example")
    fun delete(@PathVariable id: String) {
        deleteUseCase(DeleteExampleCommand(id))
    }
}
```

---

## OpenAPI Annotations

### Controller/Method Level

| Annotation | Purpose |
|------------|---------|
| `@Tag(name, description)` | Group endpoints |
| `@Operation(summary, description)` | Document endpoint |
| `@ApiResponse(responseCode, description)` | Document response |
| `@ApiResponses` | Multiple response codes |

### Schema Level

| Annotation | Purpose |
|------------|---------|
| `@Schema(description, example)` | Document field |
| `@Schema(hidden = true)` | Hide field from docs |

### Example with OpenAPI

```kotlin
data class CreateExampleRequest(
    @field:NotBlank
    @field:Schema(description = "Example name", example = "My Example")
    val name: String,
    
    @field:Schema(description = "Optional description", nullable = true)
    val description: String?
)

@Operation(
    summary = "Create example",
    description = "Creates a new example resource"
)
@ApiResponses(
    ApiResponse(responseCode = "201", description = "Created successfully"),
    ApiResponse(responseCode = "400", description = "Validation error"),
    ApiResponse(responseCode = "401", description = "Unauthorized")
)
@PostMapping
fun create(@Valid @RequestBody request: CreateExampleRequest): ExampleResponse
```

---

## Versioning

### URL Path Versioning (Recommended)

```kotlin
@RestController
@RequestMapping("/api/v1/examples")
class ExampleControllerV1 { ... }

@RestController
@RequestMapping("/api/v2/examples")
class ExampleControllerV2 { ... }
```

### Header Versioning (Alternative)

```kotlin
@GetMapping(headers = ["X-API-Version=1"])
fun getV1(): ResponseV1

@GetMapping(headers = ["X-API-Version=2"])
fun getV2(): ResponseV2
```
