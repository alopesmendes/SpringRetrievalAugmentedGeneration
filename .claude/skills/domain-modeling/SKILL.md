---
name: domain-modeling
description: Expert advisor for domain layer design in hexagonal architecture projects using Kotlin. Covers entities, value objects, domain annotations, domain exceptions, domain services, Result pattern, and validation placement. Trigger when user creates or modifies domain entities, value objects, domain services, domain exceptions, or asks where validation belongs, how to model a new concept, or whether something belongs in the domain layer.
---

# Domain Modeling Skill

Pure Kotlin. No framework. Business invariants live here.

## Annotations

Replace Spring stereotypes with project annotations (no Spring dep):

| Annotation       | Use on                           |
|------------------|----------------------------------|
| `@DomainEntity`  | Aggregate root with identity     |
| `@ValueObject`   | Immutable, identity-by-value     |
| `@DomainService` | Logic spanning multiple entities |

## Entity

Aggregate root. Has identity. Rich with behavior.

```kotlin
@DomainEntity
data class User(
    val id: UserId,
    val firstName: Name,
    val lastName: Name,
    val email: Email,
    val age: Age,
    val password: PasswordHash,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val fullName: String get() = "$firstName $lastName"

    fun update(firstName: Name? = null, email: Email? = null): User =
        copy(firstName = firstName ?: this.firstName, email = email ?: this.email, updatedAt = Instant.now())

    companion object {
        fun create(firstName: Name, lastName: Name, email: Email, age: Age, password: PasswordHash): User =
            User(UserId.generate(), firstName, lastName, email, age, password, Instant.now(), Instant.now())

        fun from(id: UserId, firstName: Name, lastName: Name, email: Email, age: Age, password: PasswordHash,
                 createdAt: Instant, updatedAt: Instant): User =
            User(id, firstName, lastName, email, age, password, createdAt, updatedAt)
    }
}
```

Rules:
- Immutable `data class`. Mutations via `copy(...)`.
- Typed VO IDs — never raw `String` or `Long`.
- `create()` for new instances (generates ID, stamps timestamps).
- `from()` for reconstitution from persistence (caller supplies all fields).
- Validation in `init` for cross-field invariants.

## Value Object

Single-field: `@JvmInline value class` — zero allocation overhead.

```kotlin
@ValueObject
@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email must not be blank" }
        require(value.length <= MAX_LENGTH) { "Email cannot exceed $MAX_LENGTH characters" }
        require(value.matches(EMAIL_REGEX)) { "Email format is invalid" }
    }

    override fun toString() = value

    companion object {
        private val EMAIL_REGEX = Regex("""^[\w-.+]+@([\w-]+\.)+[\w-]{2,4}$""")
        const val MAX_LENGTH = 254

        fun from(value: String): Email = Email(value.trim().lowercase())
    }
}
```

Multi-field VO: `data class` (identity by all fields).

Rules:
- Self-validating in `init`. No separate validator class.
- `from()` factory for normalization (trim, lowercase) before construction.
- Never expose mutable state.

## Domain Service

Use only when logic spans multiple entities or has no natural home on any entity.

```kotlin
@DomainService
class PasswordService {
    fun verify(plain: String, hash: PasswordHash): Boolean = BCrypt.checkpw(plain, hash.value)
}
```

Decision rule:
- Single entity? → method on entity.
- Needs I/O (DB, network, clock)? → output port in application, NOT a domain service.
- Spans two+ entities and pure? → domain service.

## Domain Exception

Sealed hierarchy. Framework-agnostic. Infra translates to HTTP status codes.

```kotlin
sealed class UserException(
    override val message: String,
    override val cause: Throwable?,
) : RuntimeException(message, cause)

data class UserAlreadyExistsException(val email: Email) : UserException(
    message = "User already exists with email $email",
    cause = IllegalArgumentException("User already exists with email $email"),
)

data class UserNotFoundException(val id: String) : UserException(
    message = "User not found: $id",
    cause = IllegalArgumentException("User not found: $id"),
)

data class InvalidUserDataException(override val cause: Throwable) : UserException(
    message = cause.message ?: "",
    cause = cause,
)
```

Rules:
- One sealed class per aggregate.
- Each subclass is a `data class` (carries context, equality, toString).
- Infra `when`-exhaustive fold to `ResponseEntity`. Never catch in domain.

## Validation placement

| What                         | Where                             |
|------------------------------|-----------------------------------|
| Shape (regex, length, range) | VO `init`                         |
| Cross-field invariant        | Entity `init`                     |
| Cross-entity business rule   | Use case (application layer)      |
| Input sanitization           | REST adapter (before building VO) |

Never duplicate validation across layers. VO validates once; use case trusts VO.

## Decision guide

| Question                             | Answer                     |
|--------------------------------------|----------------------------|
| New concept has identity over time?  | Entity                     |
| Equality by value, no identity?      | Value Object               |
| Single-field, performance-sensitive? | `@JvmInline value class`   |
| Multi-field equality?                | `data class` VO            |
| Logic on one entity?                 | Method on entity           |
| Logic spans entities, pure?          | Domain service             |
| Logic needs I/O?                     | Output port in application |
| Error from business rule?            | Typed domain exception     |

## What does NOT belong in domain

- Spring / Jakarta / framework annotations
- `@Document`, `@Table`, `@Id` (persistence concerns)
- Jackson `@JsonProperty` (serialization concerns)
- Any network or DB call
- `Result<T>` wrapping — domain throws; use case wraps

## Consistency checklist

Before adding a domain class:
1. Zero imports outside Kotlin stdlib + own domain package?
2. Does it carry invariants that `init` enforces?
3. Is it annotated with project annotation (`@DomainEntity`, `@ValueObject`, `@DomainService`)?
4. Konsist test will verify — run `./gradlew :domain:test` to confirm.
