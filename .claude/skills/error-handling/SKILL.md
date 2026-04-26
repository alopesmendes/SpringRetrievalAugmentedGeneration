---
name: error-handling
description: Error handling patterns for this project. Covers Result<T> usage, sealed domain exception hierarchies, where to throw vs wrap, folding at the infrastructure boundary, and never swallowing errors. Trigger when designing error paths, deciding between throw and Result, writing exception classes, handling failures in use cases or adapters, or reviewing error propagation across layers.
---

# Error Handling Skill

Typed errors. Result at boundaries. Never swallow. Never leak domain exceptions raw to HTTP.

## Two mechanisms — when to use each

| Mechanism                     | Use when                                              |
|-------------------------------|-------------------------------------------------------|
| `throw DomainException`       | Domain invariant violated (entity/VO `init`, factory) |
| `Result<T>` via `runCatching` | Use case boundary — caller decides how to handle      |
| Fold `Result<T>`              | Inbound adapter (controller) translating to HTTP      |

Never mix: don't throw inside a `runCatching` block you own, then catch silently.

## Domain exceptions — sealed hierarchy

One sealed class per aggregate root. Exhaustive `when` at fold site.

```kotlin
sealed class UserException(message: String) : RuntimeException(message) {
    class NotFound(id: UserId) : UserException("user ${id.value} not found")
    class EmailAlreadyExists(email: Email) : UserException("${email.value} already taken")
    class InvalidCredentials : UserException("invalid credentials")
}
```

Rules:
- No `Exception` or `RuntimeException` thrown from domain — only sealed subtypes.
- Message is human-readable but not sensitive (no passwords, no tokens).
- No stack-trace manipulation. Let it propagate naturally.

## Value object validation — throw in `init`

VO shape violation = programming error or bad external input. Throw immediately.

```kotlin
@JvmInline
value class Email(val value: String) {
    init { require(value.matches(EMAIL_REGEX)) { "invalid email: $value" } }
}
```

`require()` throws `IllegalArgumentException`. REST adapter catches this via global exception handler and returns 400.

## Use case boundary — `Result<T>`

Wrap the whole use case body in `runCatching`. Let domain exceptions propagate into the `Result.Failure`.

```kotlin
override fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
    repo.findByEmail(cmd.email)?.let { throw UserException.EmailAlreadyExists(cmd.email) }
    repo.save(User.create(cmd.email, cmd.name, hasher.hash(cmd.password), cmd.age)).toUserResult()
}
```

Use case never returns `null` for "not found" — throw `UserException.NotFound`.

## Fold at inbound adapter (controller)

Controller folds `Result<T>` to HTTP. Never let domain exception bubble raw.

```kotlin
@PostMapping
fun create(@RequestBody @Valid dto: CreateUserRequestDto): ResponseEntity<UserResponseDto> =
    createUser(dto.toCommand()).fold(
        onSuccess = { ResponseEntity.status(201).body(it.toResponseDto()) },
        onFailure = { it.toErrorResponse() },
    )
```

`toErrorResponse()` is an extension on `Throwable` defined in `infrastructure/error/`:

```kotlin
fun Throwable.toErrorResponse(): ResponseEntity<ErrorDto> = when (this) {
    is UserException.NotFound -> ResponseEntity.status(404).body(ErrorDto(message ?: "not found"))
    is UserException.EmailAlreadyExists -> ResponseEntity.status(409).body(ErrorDto(message ?: "conflict"))
    is IllegalArgumentException -> ResponseEntity.status(400).body(ErrorDto(message ?: "bad request"))
    else -> ResponseEntity.status(500).body(ErrorDto("internal error"))
}
```

`when` must be exhaustive — add new `UserException` subtype → compiler forces fold update.

## Output port errors — outbound adapter

Adapters implement output ports. They may throw infrastructure exceptions (Mongo timeout, network error). Wrap in a domain-meaningful exception or let it propagate as unknown failure.

```kotlin
override fun save(user: User): User = try {
    mongo.save(user.toDocument()).toDomain()
} catch (e: DuplicateKeyException) {
    throw UserException.EmailAlreadyExists(user.email)
}
```

Never expose `MongoException`, `SQLException`, or framework-specific types to the use case.

## Never swallow

```kotlin
// bad
try {
    repo.save(user)
} catch (e: Exception) {
    logger.error("oops") // swallowed — caller sees success
}

// bad
runCatching { repo.save(user) }.getOrNull() // null = silent failure

// good
runCatching { repo.save(user) }.getOrThrow() // re-throws if needed
// or propagate Result to caller
```

## Logging errors

Log at the boundary where you handle (fold site), not where you throw.

```kotlin
// in exception handler / controller advice
logger.warn("user creation failed: ${e.message}")
```

Never log + rethrow — double-logging confuses tracing.

## Anti-patterns

| Pattern                                   | Problem                       | Fix                        |
|-------------------------------------------|-------------------------------|----------------------------|
| `catch (e: Exception) { }`                | Swallows all errors           | Handle or rethrow typed    |
| Domain throws `RuntimeException` directly | Untyped, hard to map          | Use sealed subtype         |
| Controller `throws` declaration           | Leaks domain type             | Fold to HTTP in controller |
| `Optional<T>` return                      | Java idiom, awkward in Kotlin | Use `T?` or throw          |
| Multiple catch blocks for same error type | Duplication                   | Consolidate in `when`      |
