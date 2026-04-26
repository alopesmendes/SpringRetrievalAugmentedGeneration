---
name: kotlin-style
description: Kotlin style and idiom guide for this project. Covers naming conventions, data classes, inline value classes, sealed hierarchies, extension functions, scope functions, and backtick test names. Trigger when writing or reviewing Kotlin code, naming a new class/interface/file, deciding between data class vs plain class, handling nullability, or writing test method names.
---

# Kotlin Style Skill

Idiomatic Kotlin. No boilerplate. Names reveal intent.

## Naming conventions (project-wide)

| Thing        | Convention                                      | Example                                       |
|--------------|-------------------------------------------------|-----------------------------------------------|
| Interfaces   | `I` prefix                                      | `IUserRepository`, `ICreateUserUseCase`       |
| Use cases    | `{Action}{Entity}UseCase`                       | `CreateUserUseCase`, `GetUserUseCase`         |
| Mappers      | `{Layer}{Entity}Mapper` (file) or extension fns | `UserRestMapper.kt`                           |
| DTOs         | `{Entity}{Purpose}Dto`                          | `CreateUserRequestDto`, `UserResponseDto`     |
| Mongo docs   | `{Entity}Document`                              | `UserDocument`                                |
| Commands     | `{Action}{Entity}Command`                       | `CreateUserCommand`                           |
| Results      | `{Entity}Result`                                | `UserResult`                                  |
| Test methods | backtick given/when/then                        | `` `given valid email when create then ok` `` |
| Test files   | `{Class}Test.kt` / `{Class}IntegrationTest.kt`  |                                               |
| Packages     | lowercase, no hyphens                           | `user.valueobjects`                           |

## Entities — `data class`

```kotlin
@DomainEntity
data class User(
    val id: UserId,
    val email: Email,
    val name: Name,
    val passwordHash: PasswordHash,
    val age: Age,
) {
    init { /* cross-field invariants via require() */ }

    companion object {
        fun create(email: Email, name: Name, hash: PasswordHash, age: Age): User =
            User(UserId.generate(), email, name, hash, age)
    }
}
```

- All fields `val`. Mutations via `copy(...)`.
- `companion object` factory named `create()` for public construction.
- `init` for invariants — not in factory.

## Value objects — `@JvmInline value class`

Single-field VO: inline value class (zero allocation at runtime).

```kotlin
@ValueObject
@JvmInline
value class Email(val value: String) {
    init { require(value.matches(EMAIL_REGEX)) { "invalid email: $value" } }
    companion object { private val EMAIL_REGEX = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$") }
}
```

Multi-field VO: `data class`, still immutable.

```kotlin
@ValueObject
data class FullName(val first: String, val last: String) {
    init {
        require(first.isNotBlank()) { "first name blank" }
        require(last.isNotBlank()) { "last name blank" }
    }
}
```

## Sealed classes — domain exceptions

```kotlin
sealed class UserException(message: String) : RuntimeException(message) {
    class NotFound(id: UserId) : UserException("user ${id.value} not found")
    class EmailAlreadyExists(email: Email) : UserException("${email.value} already taken")
}
```

Use `when` exhaustively — compiler enforces coverage:

```kotlin
when (e) {
    is UserException.NotFound -> ResponseEntity.notFound().build()
    is UserException.EmailAlreadyExists -> ResponseEntity.status(409).build()
}
```

## Extension functions — mappers

Prefer extension functions over mapper classes. One file per boundary.

```kotlin
// application/mapper/UserMapper.kt
fun User.toUserResult(): UserResult = UserResult(id.value, email.value, name.value, age.value)

// infrastructure/adapter/primary/rest/mapper/UserRestMapper.kt
fun CreateUserRequestDto.toCommand(): CreateUserCommand =
    CreateUserCommand(Email(email), Name(name), password, Age(age))

fun UserResult.toResponseDto(): UserResponseDto = UserResponseDto(id, email, name, age)
```

## Nullability

- Prefer `?` over `Optional<T>`.
- Use `?.let { }`, `?: return`, `?: throw` over explicit null checks.
- Never return `null` from domain — throw typed exception or return `Result`.

```kotlin
// prefer
fun findByEmail(email: Email): User?

// use at call site
repo.findByEmail(cmd.email)?.let { throw UserException.EmailAlreadyExists(it.email) }
```

## Scope functions — use sparingly

| Function | Receiver | Returns       | Use                           |
|----------|----------|---------------|-------------------------------|
| `let`    | `it`     | lambda result | null-safe transform           |
| `also`   | `it`     | receiver      | side effect (logging)         |
| `apply`  | `this`   | receiver      | builder-style init            |
| `run`    | `this`   | lambda result | object config + result        |
| `with`   | `this`   | lambda result | multi-call on non-null object |

Avoid nesting scope functions — unreadable.

## `Result<T>` idiom

```kotlin
// produce
fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
    // throws domain exception on failure
    val user = User.create(...)
    repo.save(user).toUserResult()
}

// consume (at infra boundary)
useCase(cmd).fold(
    onSuccess = { ResponseEntity.status(201).body(it.toResponseDto()) },
    onFailure = { it.toErrorResponse() },
)
```

## What NOT to do

- No `!!` operator except in tests with known-good data.
- No `lateinit var` in domain or application — use constructor injection.
- No `object` singletons with mutable state.
- No utility classes — use extension functions or companion objects.
- No raw `String` IDs crossing layer boundaries — wrap in typed VO.
