# Project Rules & Conventions

## Naming Conventions

| Type             | Convention                           | Example                                                  |
|------------------|--------------------------------------|----------------------------------------------------------|
| Interface        | `I{Name}`                            | `IUserRepository`, `IAnalyzeImageUseCase`                |
| Use Case         | `{Action}{Entity}UseCase`            | `AnalyzeImageUseCase`, `CreateUserUseCase`               |
| Service          | `{Name}Service`                      | `PasswordService`, `TokenService`                        |
| Controller       | `{Entity}Controller`                 | `ImageController`, `UserController`                      |
| Repository       | `{Entity}Repository`                 | `ImageRepository`, `UserRepository`                      |
| DTO              | `{Entity}{Purpose}Dto`               | `ImageRequestDto`, `UserResponseDto`                     |
| Command          | `{Action}{Entity}Command`            | `AnalyzeImageCommand`, `CreateUserCommand`               |
| Result           | `{Entity}Result` or `{Action}Result` | `UserResult`, `AuthResult`                               |
| Entity (MongoDB) | `{Entity}Entity`                     | `ImageEntity`, `UserEntity`                              |
| Value Object     | `{Name}` (descriptive noun)          | `Email`, `UserId`, `PasswordHash`                        |
| Mapper           | `{Source}.to{Target}()`              | `User.toUserResponseDto()`, `ImageRequestDto.toDomain()` |

---

## Architecture Rules

### Layer Dependencies

```
Infrastructure → Application → Domain
```

- **Domain**: NO imports from Application or Infrastructure
- **Application**: NO imports from Infrastructure
- **Infrastructure**: Can import from all layers

### Framework Independence

- **Domain layer**: Zero Spring dependencies
- **Application layer**: Zero Spring dependencies
- **Infrastructure layer**: All Spring annotations live here only

### Enforcement

- Use ArchUnit tests to validate dependency rules at build time

---

## Code Style

### Use Cases

Use the `invoke` operator for single-method execution:

```kotlin
interface ICreateUserUseCase {
    operator fun invoke(command: CreateUserCommand): Result<UserResult>
}

class CreateUserUseCase : ICreateUserUseCase {
    override operator fun invoke(command: CreateUserCommand): Result<UserResult> {
        // implementation
    }
}
```

### Value Objects

Use `init` block for validation and companion `of` or `from` for factory methods:

```kotlin
@JvmInline
value class Email(value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email format" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
        fun from(value: String): Email = Email(value)
    }
}
```

### Domain Entities

Use `copy()` for modifications and factory methods for creation:

```kotlin
data class User() {
    fun updateName(newName: String): User = copy(name = newName, updatedAt = Instant.now())

    companion object {
        fun create(name: String, email: Email): User {  }
    }
}
```

---

## Use Case Guidelines

- Each use case handles **one specific business operation**
- Use cases have **only one public method** using the `invoke` operator
- Input: Command objects → Output: `Result<T>` objects
- Composite use cases can orchestrate multiple use cases without duplicating logic

---

## Exception Handling

### Domain Exceptions

```kotlin
sealed class DomainException(message: String) : RuntimeException(message)

class EntityNotFoundException(entity: String, id: String) :
    DomainException("$entity with id $id not found")

class InvalidEntityException(message: String) :
    DomainException(message)
```

### Infrastructure

- Use `@ControllerAdvice` for global exception handling
- Translate domain exceptions to HTTP status codes
- Never expose internal exception details to clients

---

## Documentation

### KDoc

- All public interfaces must have KDoc
- Document parameters and return types
- Include usage examples for complex APIs

```kotlin
/**
 * Analyzes an image and extracts information using AI.
 *
 * @param command contains the image data and analysis parameters
 * @return analysis result with extracted information
 * @throws InvalidImageException if the image format is not supported
 */
operator fun invoke(command: AnalyzeImageCommand): AnalysisResult
```

### OpenAPI

- All REST endpoints must have OpenAPI annotations
- Document request/response schemas
- Include example values
- Document error responses
