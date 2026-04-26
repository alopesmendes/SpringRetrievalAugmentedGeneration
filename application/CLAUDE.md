# application/CLAUDE.md

Use case orchestration. Pure Kotlin. Defines ports infra implements.

## Hard rules

- **No Spring imports. Ever.** Same rule as domain.
- Depends only on `domain/` + Kotlin stdlib.
- No framework annotations on classes (plain `class`, no `@Service`).
- Konsist tests enforce — failures = stop.

## Folder layout

```
application/src/main/kotlin/{feature}/
  dto/          — Command (input) + Result (output) types
  mapper/       — domain ↔ Result mappers (extension fns)
  port/
    primary/    — IxxxUseCase (input ports — what app does)
    secondary/  — Ixxx (output ports — what app needs from infra)
  useCases/     — concrete use case impls
```

Existing: `user/{dto, mapper, port/{primary,secondary}, useCases}`.

## Patterns

### Input port + use case
```kotlin
// port/primary/ICreateUserUseCase.kt
interface ICreateUserUseCase {
    operator fun invoke(command: CreateUserCommand): Result<UserResult>
}

// useCases/CreateUserUseCase.kt
class CreateUserUseCase(
    private val repo: IUserRepository,
    private val hasher: IPasswordHasherService,
) : ICreateUserUseCase {
    override operator fun invoke(command: CreateUserCommand): Result<UserResult> = runCatching {
        repo.findByEmail(command.email)?.let { throw UserException.EmailAlreadyExists(it.email) }
        val user = User.create(command.email, command.name, hasher.hash(command.password), command.age)
        repo.save(user).toUserResult()
    }
}
```

- One public method via `operator fun invoke`.
- Inputs = `Command`. Outputs = `Result` wrapper around DTO.
- Constructor injection. Plain class — Spring wires via config bean in infra.

### Output port
Defined here, implemented in infra. Domain types in/out — never DTO.

```kotlin
// port/secondary/IUserRepository.kt
interface IUserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun delete(id: UserId)
}
```

### Command / Result DTOs
Plain data classes. No infra concerns (no Mongo annotations, no Jackson).

```kotlin
data class CreateUserCommand(val email: Email, val name: Name, val password: String, val age: Age)
data class UserResult(val id: String, val email: String, val name: String, val age: Int)
```

Use VOs in commands when validation should happen at adapter boundary (REST mapper builds VOs, fails fast).

### Mapper
Extension on domain entity → Result.
```kotlin
// mapper/UserMapper.kt
fun User.toUserResult(): UserResult = UserResult(id.value, email.value, name.value, age.value)
```

## Decisions

- **VO in Command or raw types?** Project default: VOs. Adapter constructs them — REST mapper rejects bad input early. Use raw only when adapter cannot construct (e.g. multi-step parsing).
- **Composite use cases?** OK. One use case calling another via interface. No logic duplication. Example: `RegisterAndLoginUseCase` calls `IRegisterUserUseCase` + `ILoginUserUseCase`.
- **Where to fold Result?** Infra (REST controller). Application returns `Result<T>`, never `ResponseEntity`.

## Tests

Location: `application/src/test/kotlin/{feature}/useCases/{Class}Test.kt`.

- MockK output ports (`IUserRepository`, etc.).
- Real domain entities — never mock.
- Verify port interactions + Result correctness.
- Backtick names.

## When to ask user

- New use case → ask: command shape? side effects? failure modes (typed exceptions)?
- New output port → ask: which adapter implements? sync or async? error semantics?
- VO in command vs raw → ask if unclear.
- Adding a dep → STOP. Application takes only Kotlin stdlib + domain.

## Skill triggers

`hexagonal-architecture`, `api-design`, `error-handling`, `kotlin-style`, `testing-strategy`.

## Doc index

`application/docs/GRAPH.md` (synced by `doc-graph-updater`).
