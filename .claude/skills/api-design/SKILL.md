---
name: api-design
description: Port-level interface contract design for hexagonal architecture. Layer-agnostic. Covers input ports (use case interfaces), output ports (repository/service interfaces), commands, results, and application-layer mappers. Trigger when user designs a new use case, defines a repository interface, creates command/result DTOs, adds an output port for an external service, or asks how data flows between layers.
---

# API Design Skill

Ports define contracts. Adapters implement them. Use cases orchestrate through ports.

## Input port (primary / driving)

What the application offers. Defined in `application/port/primary/`. Implemented by use case.

```kotlin
// port/primary/ICreateUserUseCase.kt
fun interface ICreateUserUseCase {
    operator fun invoke(command: CreateUserCommand): Result<UserResult>
}
```

Rules:
- `fun interface` when single abstract method (SAM). Enables lambda injection in tests.
- `operator fun invoke` — call site reads `useCase(command)`.
- Input = `Command` (plain data, no domain entity).
- Output = `Result<XxxResult>` — never `ResponseEntity`, never raw entity.
- `I` prefix: `ICreateUserUseCase`, `IGetUserUseCase`.

## Output port (secondary / driven)

What the application needs from infra. Defined in `application/port/secondary/`. Implemented in infra.

```kotlin
// port/secondary/IUserRepository.kt
interface IUserRepository {
    fun existsByEmail(email: Email): Boolean
    fun save(user: User): User
    fun findById(id: UserId): User?
}
```

Rules:
- Domain types in and out — never DTO, never persistence document.
- Return `null` (not exception) for not-found lookups. Use case decides whether to throw.
- Name by what it does, not which adapter implements it (`IUserRepository`, not `IMongoUserRepository`).
- One interface per aggregate, not per use case. Methods = what use cases need.

## Command DTO

Input crossing the adapter → application boundary. Carries raw or VO-typed fields.

```kotlin
// dto/CreateUserCommand.kt
data class CreateUserCommand(
    val email: String,       // raw: adapter may not have VO context yet
    val age: Int,
    val rawPassword: String,
    val firstName: String,
    val lastName: String,
)
```

When to use raw types vs VOs in commands:

| Use raw when | Use VO when |
|--------------|-------------|
| Adapter can't construct VO (multi-step parsing) | Adapter can fail-fast at boundary |
| Command spans partial data (patch update) | Full entity creation |

Project default: raw types in commands. Use case constructs VOs → fail fast inside use case.

## Result DTO

Output crossing application → adapter boundary. Flat, serialization-friendly.

```kotlin
// dto/UserResult.kt
data class UserResult(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
)
```

Rules:
- No domain types. No VOs. Flat primitives or strings only.
- No Jackson/Mongo annotations — infra adds those at REST layer if needed.
- Named `{Entity}Result` for read results. Can also have `{Action}Result` for action outcomes.

## Application-layer mapper

Extension function on domain entity → Result. Never inside use case.

```kotlin
// mapper/UserMapper.kt
fun User.toUserResult(): UserResult = UserResult(
    id = id.value.toString(),
    email = email.value,
    firstName = firstName.value,
    lastName = lastName.value,
    age = age.value,
)
```

Rules:
- Extension function, not a class. Reads as `user.toUserResult()`.
- One mapper file per aggregate (`UserMapper.kt`).
- No reverse direction here — `Result → domain` never happens in application mapper.
- REST mapper (in infra) handles `RequestDto → Command` and `Result → ResponseDto`.

## Use case implementation

```kotlin
// useCases/CreateUserUseCase.kt
class CreateUserUseCase(
    private val repo: IUserRepository,
    private val hasher: IPasswordHasherService,
) : ICreateUserUseCase {
    override operator fun invoke(command: CreateUserCommand): Result<UserResult> = runCatching {
        if (repo.existsByEmail(Email.from(command.email))) throw UserAlreadyExistsException(Email.from(command.email))
        val user = User.create(
            firstName = Name(command.firstName),
            lastName = Name(command.lastName),
            email = Email.from(command.email),
            age = Age(command.age),
            password = hasher.hash(command.rawPassword),
        )
        repo.save(user).toUserResult()
    }
}
```

Rules:
- Plain class. No `@Service`. Spring wires via `@Bean` in infra config.
- Constructor injection of ports only — never concrete adapters.
- `runCatching` wraps the body → `Result<T>` boundary.
- Construct VOs from raw command fields here (or earlier at REST adapter — be consistent).

## Wiring (infra config — for reference)

```kotlin
@Configuration
class UserBeanConfig {
    @Bean
    fun createUserUseCase(repo: IUserRepository, hasher: IPasswordHasherService): ICreateUserUseCase =
        CreateUserUseCase(repo, hasher)
}
```

Use case impl stays unaware of Spring. Bean config lives in infra.

## Data flow summary

```
REST request
  → RequestDto (infra)
  → Command (application)
  → Domain entity via User.create()
  → Output port (IUserRepository.save)
  → Domain entity returned
  → Result via .toUserResult()
  → ResponseDto (infra)
  → HTTP response
```

## Decision guide

| Question                        | Answer                                               |
|---------------------------------|------------------------------------------------------|
| New operation on an aggregate?  | New input port + use case                            |
| App needs something from infra? | New output port in application                       |
| Read-only lookup?               | `findById` returns `T?`; use case throws if null     |
| Cross-aggregate orchestration?  | Single use case calling multiple output ports        |
| Composite use case?             | Use case calls other use cases via their input ports |
| VO in command?                  | Ask: can the adapter build it without ambiguity?     |

## What does NOT belong in ports/use cases

- HTTP status codes, `ResponseEntity` — infra only
- `@Transactional`, `@Cacheable` — infra annotations
- Mongo types (`MongoRepository`, `@Document`) — infra only
- Business logic in output port method — logic in use case, port = I/O contract only

## Consistency checklist

Before adding a port or use case:
1. Port defined in `application/port/{primary|secondary}/`?
2. Port methods use domain types (not DTOs, not persistence types)?
3. Use case is a plain class (no framework annotations)?
4. Input port returns `Result<XxxResult>`, not raw entity?
5. Konsist enforces — run `./gradlew :application:test` to confirm.
