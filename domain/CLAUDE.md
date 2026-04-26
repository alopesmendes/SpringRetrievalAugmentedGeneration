# domain/CLAUDE.md

Pure Kotlin core. Zero framework imports. Business invariants live here.

## Hard rules

- **No Spring imports. Ever.** No `org.springframework.*`, no `jakarta.*`, no Spring Data, no Mongo annotations.
- Only Kotlin stdlib + project's own domain code. External deps go through ports defined in `application/`.
- Konsist tests in `domain/src/test/kotlin/architecture/` enforce this — failures = stop.

## Folder layout

```
domain/src/main/kotlin/{feature}/
  entity/         — aggregates with identity
  valueobjects/   — immutable, no identity
  services/       — domain logic spanning multiple entities
  exception/      — sealed class hierarchies
  annotation/     — project's @DomainEntity, @ValueObject, @DomainService (replace Spring)
```

Existing: `user/{entity, valueobjects, services, exception}`.

## Patterns

### Entity
```kotlin
@DomainEntity
data class User(
    val id: UserId,
    val email: Email,
    val name: Name,
    val passwordHash: PasswordHash,
    val age: Age,
) {
    init { /* invariants via require(...) */ }

    companion object {
        fun create(email: Email, name: Name, hash: PasswordHash, age: Age): User =
            User(UserId.generate(), email, name, hash, age)
    }
}
```

- Immutable `data class`.
- Typed VO IDs. No raw `String` ids.
- Validation in `init` block. Factory `create()` for new instances.
- Mutations via `copy(...)`.

### Value object
```kotlin
@ValueObject
@JvmInline
value class Email(val value: String) {
    init { require(value.matches(EMAIL_REGEX)) { "invalid email" } }

    companion object { private val EMAIL_REGEX = Regex(...) }
}
```

- `@JvmInline value class` for single-field VOs (zero allocation).
- `data class` for multi-field VOs.
- Self-validating in `init`.

### Domain service
Use only when logic spans multiple entities or has no natural home. Single-entity logic → method on entity.

```kotlin
@DomainService
class PasswordService { fun verify(plain: String, hash: PasswordHash): Boolean = ... }
```

### Domain exception
Sealed hierarchy. Framework-agnostic. Infra translates to HTTP.

```kotlin
sealed class UserException(message: String) : RuntimeException(message) {
    class NotFound(id: UserId) : UserException("user $id not found")
    class EmailAlreadyExists(email: Email) : UserException("$email taken")
}
```

## Decisions

- **Where does validation go?** VO `init` for shape (regex, length, range). Entity `init` for cross-field invariants. Use case for cross-entity rules.
- **Anemic vs rich?** Rich. Behavior on entity. Use case orchestrates only.
- **Result<T> vs throw?** Either. Be consistent within a feature. Project default: throw `UserException` from entity factory; wrap in `Result<T>` at use case boundary.

## Tests

Location: `domain/src/test/kotlin/{feature}/{type}/{Class}Test.kt`.

- Pure unit tests. No mocks of domain objects (they're easy to construct).
- MockK only for ports — but ports live in application, so domain tests rarely need it.
- One test per invariant. Backtick names: `` `given empty email when create then throws` ``.

## When to ask user

- New entity → ask: which invariants? identity strategy (generate vs caller-supplied)? mutability points?
- New VO → ask: validation rules (regex, range, length)? equality semantics?
- New domain service → ask: why not on the entity? which entities does it span?
- Adding a dep → STOP. Domain takes only Kotlin stdlib. Push the dep into a port.

## Skill triggers (model auto-loads)

`hexagonal-architecture`, `domain-modeling`, `kotlin-style`, `error-handling`, `testing-strategy`.

## Doc index

Per-feature docs live at `domain/docs/{feature}/`. Master index: `domain/docs/GRAPH.md` (kept in sync by `doc-graph-updater` agent).
