# Domain Model Guidelines

## Entity Structure

Entities are domain objects with identity that persists over time.

### Required Fields
Every entity **must** have:
- `id` - Typed identifier (value object, not primitive)
- `createdAt` - Creation timestamp
- `updatedAt` - Last modification timestamp

### Pattern

```kotlin
@DomainEntity
data class Example(
    val id: ExampleId,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

    fun updateName(newName: String): Example {
        return copy(
            name = newName,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(name: String): Example {
            val now = Instant.now()
            return Example(
                id = ExampleId.generate(),
                name = name,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
```

### Rules
- Use `data class` for automatic `equals`, `hashCode`, `copy`
- Keep entities **immutable** - return new instance on modifications
- Factory method (`create`) handles default values
- Update methods return new instance with updated `updatedAt`
- Validation in `init` block

---

## Value Object Structure

Value objects are immutable objects defined by their attributes, not identity.

### Typed Identifiers

```kotlin
@JvmInline
@ValueObject
value class ExampleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Id cannot be blank" }
    }

    companion object {
        fun generate(): ExampleId = ExampleId(UUID.randomUUID().toString())
        fun from(value: String): ExampleId = ExampleId(value)
    }
}
```

### Complex Value Objects

```kotlin
@ValueObject
data class Address(
    val street: String,
    val city: String,
    val zipCode: String
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(zipCode.matches(Regex("\\d{5}"))) { "Invalid zip code" }
    }
}
```

### Rules
- Use `@JvmInline value class` for single-value wrappers (type safety, zero overhead)
- Use `data class` for multi-field value objects
- Validation **always** in `init` block
- No setters, no mutable state
- Equality based on all fields (automatic with data class)

---

## Domain Annotations

Custom annotations to mark domain concepts without framework dependencies:

```kotlin
// domain/annotation/DomainEntity.kt
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DomainEntity

// domain/annotation/ValueObject.kt
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueObject

// domain/annotation/DomainService.kt
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DomainService
```

---

## Domain Exceptions

Use sealed classes for grouping related exceptions:

```kotlin
sealed class DomainException(message: String) : RuntimeException(message)

// Not found exceptions
sealed class NotFoundException(message: String) : DomainException(message)

data class EntityNotFoundException(val entity: String, val id: String) :
    NotFoundException("$entity with id $id not found")

// Validation exceptions
sealed class ValidationException(message: String) : DomainException(message)

data class InvalidEntityException(override val message: String) :
    ValidationException(message)

data class InvalidOperationException(override val message: String) :
    ValidationException(message)
```

### Result Pattern

Use Kotlin `Result<T>` for error handling across layers:

```kotlin
// Domain layer - returns Result
@DomainService
class ExampleDomainService {

    fun validate(entity: Entity): Result<Entity> {
        return if (entity.isValid()) {
            Result.success(entity)
        } else {
            Result.failure(InvalidEntityException("Entity validation failed"))
        }
    }
}

// Application layer - use fold
class ExampleUseCase(
    private val repository: IExampleRepository,
    private val domainService: ExampleDomainService
) : IExampleUseCase {

    override operator fun invoke(command: ExampleCommand): Result<ExampleResult> {
        return domainService.validate(command.toEntity())
            .map { entity -> repository.save(entity) }
            .map { saved -> saved.toResult() }
    }
}

// Infrastructure layer - fold to HTTP response
@RestController
class ExampleController(private val useCase: IExampleUseCase) {

    @PostMapping
    fun create(@RequestBody request: ExampleRequest): ResponseEntity<Any> {
        return useCase(request.toCommand()).fold(
            onSuccess = { result -> ResponseEntity.ok(result.toResponse()) },
            onFailure = { error ->
                when (error) {
                    is NotFoundException -> ResponseEntity.notFound().build()
                    is ValidationException -> ResponseEntity.badRequest().body(error.message)
                    else -> ResponseEntity.internalServerError().build()
                }
            }
        )
    }
}
```

### Benefits
- Explicit error handling
- No unexpected exceptions
- Type-safe error propagation
- Clean mapping with `fold`, `map`, `mapCatching`

### Benefits
- Exhaustive `when` expressions
- Clear exception hierarchy
- Framework-agnostic
- Easy mapping to HTTP status codes in infrastructure layer

---

## Validation Rules

### Where Validation Lives

| Type                   | Location            | Purpose                           |
|------------------------|---------------------|-----------------------------------|
| Format validation      | Value Object `init` | Email format, ID format           |
| Business invariants    | Entity `init`       | Required fields, constraints      |
| Cross-entity rules     | Domain Service      | Rules involving multiple entities |
| Use case preconditions | Use Case            | Authorization, existence checks   |

### Pattern

```kotlin
@ValueObject
data class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email format" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    }
}

@DomainEntity
data class User(
    val id: UserId,
    val email: Email,  // Format already validated
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.length in 2..100) { "Name must be between 2 and 100 characters" }
    }
}
```

---

## Domain Services

Use domain services when logic:
- Doesn't naturally belong to a single entity
- Involves multiple entities
- Requires complex calculations

```kotlin
@DomainService
class ExampleDomainService {

    fun calculateSomething(entity1: Entity1, entity2: Entity2): Result {
        // Cross-entity business logic
    }
}
```

### Rules
- Stateless
- Pure functions when possible
- No infrastructure dependencies
- Named after the operation they perform
