# Project Rules & Conventions

## Naming Conventions

### Interfaces
- All interfaces **must** start with `I` prefix
- Examples: `IUserRepository`, `IAnalyzeImageUseCase`, `IAuthService`

### Tests
- Test methods follow `given...when...then` pattern
- Format: `givenCondition_whenAction_thenExpectedResult`
- Examples:
  - `givenValidImage_whenAnalyze_thenReturnsAnalysisResult`
  - `givenInvalidUserId_whenGetUser_thenThrowsNotFoundException`

### Classes & Files
- Use cases: `{Action}{Entity}UseCase` (e.g., `AnalyzeImageUseCase`)
- Repositories: `{Entity}Repository` (e.g., `ImageRepository`)
- Mappers: `{Source}To{Target}Mapper` or `{Layer}{Entity}Mapper` (e.g., `RestImageMapper`)
- DTOs: `{Entity}{Purpose}Dto` (e.g., `ImageRequestDto`, `ImageResponseDto`)
- Documents (MongoDB): `{Entity}Document` (e.g., `ImageDocument`)

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

### Kotlin Conventions
- Prefer `val` over `var` (immutability first)
- Use data classes for DTOs and Value Objects
- Leverage Kotlin null safety (`?`, `?.`, `?:`, `!!` sparingly)
- Use sealed classes for domain exceptions

### Immutability
- Domain entities should be immutable when possible
- Use `copy()` for modifications
- Collections should be immutable (`List`, not `MutableList`)

### Null Safety
- Avoid `!!` operator - use safe calls or explicit handling
- Use `?:` (Elvis operator) for default values
- Repository `findById` returns nullable type

---

## Use Case Guidelines

### Single Responsibility
- Each use case handles **one specific business operation**
- Use cases should have **only one public method** using the `invoke` operator

```kotlin
interface IAnalyzeImageUseCase {
    operator fun invoke(command: AnalyzeImageCommand): AnalysisResult
}
```

### Composite Use Cases
- It's acceptable to combine multiple use cases into one composite use case
- Composite use cases orchestrate other use cases without duplicating logic

```kotlin
// Individual use cases
class RegisterUserUseCase : IRegisterUserUseCase { ... }
class LoginUserUseCase : ILoginUserUseCase { ... }

// Composite use case combining both
class RegisterAndLoginUseCase(
    private val registerUseCase: IRegisterUserUseCase,
    private val loginUseCase: ILoginUserUseCase
) : IRegisterAndLoginUseCase {
    
    override operator fun invoke(command: RegisterAndLoginCommand): AuthResult {
        val user = registerUseCase(command.toRegisterCommand())
        return loginUseCase(command.toLoginCommand())
    }
}
```

### Command/Result Pattern
- Input: Command objects (e.g., `AnalyzeImageCommand`)
- Output: Result objects (e.g., `AnalysisResult`)
- No direct use of infrastructure DTOs in use cases

---

## Mapper Rules

### Location
- REST mappers: `infrastructure/adapter/inbound/rest/mapper/`
- Persistence mappers: `infrastructure/adapter/outbound/persistence/mapper/`
- AI mappers: `infrastructure/adapter/outbound/ai/mapper/`

### Responsibilities
- Mappers handle conversion between layers
- No business logic in mappers
- Each mapper handles one direction or is clearly named for bidirectional

### Extension Functions
- Mapper methods should be **extension functions** of the source class

```kotlin
// In RestImageMapper.kt
fun Image.toImageResponseDto(): ImageResponseDto {
    return ImageResponseDto(
        id = this.id.value,
        url = this.url,
        analyzedAt = this.analyzedAt
    )
}

fun ImageRequestDto.toDomain(): Image {
    return Image(
        id = ImageId.generate(),
        url = this.url
    )
}
```

### Boundary Mapping
```
REST DTO ↔ Application DTO ↔ Domain Entity ↔ Persistence Document
```

---

## Exception Handling

### Domain Exceptions
- Define in `domain/exception/`
- Use sealed classes for grouping related exceptions
- Should be framework-agnostic

```kotlin
sealed class DomainException(message: String) : RuntimeException(message)

class EntityNotFoundException(entity: String, id: String) : 
    DomainException("$entity with id $id not found")

class InvalidEntityException(message: String) : 
    DomainException(message)
```

### Infrastructure Exception Handling
- Translate domain exceptions to HTTP status codes in REST layer
- Use `@ControllerAdvice` for global exception handling
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
