# infrastructure/CLAUDE.md

All Spring + framework code lives here. Adapters implement ports from `application/`.

This module is **swappable**: Ktor, Micronaut, etc. all valid. Domain + application untouched.

## Folder layout

```
infrastructure/src/main/kotlin/{feature}/
  adapter/
    primary/
      rest/
        dto/        — request + response DTOs (Jackson)
        mapper/     — DTO ↔ Command/Result extension fns
        {Entity}Controller.kt
    secondary/
      persistence/
        document/   — Mongo @Document classes
        mapper/     — Document ↔ domain entity
        Mongo{Entity}Repository.kt  — Spring Data interface
        {Entity}Repository.kt        — adapter impl of IxxxRepository port
      security/     — BCrypt, JWT services impl ports
  config/           — Spring @Configuration (bean wiring for use cases)
```

Existing: `user/{adapter/{primary/rest, secondary/{persistence, security}}, config}`.

Cross-cutting (not feature-scoped): `config/`, `error/`.

## Patterns

### REST controller
```kotlin
@RestController
@RequestMapping("/users")
class UserController(
    private val createUser: ICreateUserUseCase,
    private val getUser: IGetUserUseCase,
) {
    @PostMapping
    fun create(@RequestBody @Valid dto: CreateUserRequestDto): ResponseEntity<UserResponseDto> =
        createUser(dto.toCommand()).fold(
            onSuccess = { ResponseEntity.status(CREATED).body(it.toResponseDto()) },
            onFailure = { it.toErrorResponse() },
        )
}
```

- Inject input ports (interfaces), not impls.
- Body validation via `@Valid` + Jakarta annotations on DTO.
- Fold `Result<T>` at controller. Never let domain exceptions bubble raw.

### Persistence adapter
```kotlin
@Repository
class UserRepository(private val mongo: MongoUserRepository) : IUserRepository {
    override fun save(user: User): User = mongo.save(user.toDocument()).toDomain()
    override fun findByEmail(email: Email): User? = mongo.findByEmail(email.value)?.toDomain()
}

@Repository interface MongoUserRepository : MongoRepository<UserDocument, String> {
    fun findByEmail(email: String): UserDocument?
}
```

Two classes: Spring Data interface (`MongoUserRepository`) + adapter impl of port (`UserRepository`).

### Bean wiring
```kotlin
// config/UserBeanConfig.kt
@Configuration
class UserBeanConfig {
    @Bean fun createUserUseCase(repo: IUserRepository, hasher: IPasswordHasherService): ICreateUserUseCase =
        CreateUserUseCase(repo, hasher)
}
```

Use cases are plain classes (defined in `application/`) — Spring instantiates via `@Configuration` bean since application module has no `@Service`.

## Tests

Three flavors live in this module:

| Type        | Location                                    | Run via                          | Needs                        |
|-------------|---------------------------------------------|----------------------------------|------------------------------|
| Unit        | `src/test/kotlin/{feature}/adapter/...`     | `./gradlew :infrastructure:test` | nothing — mappers, no Spring |
| Integration | `src/test/kotlin/{feature}/integration/...` | `./gradlew integrationTest`      | Docker (Testcontainers)      |
| E2E         | `src/test/kotlin/.../e2e/...` (Cucumber)    | `./gradlew e2eTest`              | full app + Mongo             |

Integration tests extend `AbstractMongoIntegrationTest` (Testcontainer wiring).

## Hard rules

- Domain logic NEVER in adapter, controller, or config. Adapter = translate + delegate.
- Mappers always extension fns. Never new helper class.
- Controllers never throw — always fold `Result<T>`.
- Secrets via env vars + typed `@ConfigurationProperties`. Never hardcoded.
- New external dep → wraps a port from `application/`. Never call from controller directly.

## Stack-specific notes

- **Spring Boot 4** — uses `jakarta.*`, not `javax.*`.
- **Spring AI 1.1** — `ChatClient`, `EmbeddingModel`. Wrap behind a port.
- **MongoDB** — `@Document`, `@Indexed`. Compose indexes via `@CompoundIndex`.
- **springdoc-openapi 3** — `@Operation`, `@ApiResponse` on controllers.
- **Spring Security** — filter chain in `config/SecurityConfig.kt`. Method-level via `@PreAuthorize`.

## When to ask user

- New endpoint → ask: HTTP verb? path? auth required? error response shapes?
- New external service → ask: ports name? sync/async? retry policy? credentials source?
- Mongo schema → ask: indexes? unique constraints? embedded vs referenced?
- Bean config → ask: profile-scoped (`@Profile`)? conditional (`@ConditionalOnProperty`)?

## Skill triggers

`spring-boot-config`, `spring-rest-mvc`, `spring-data-mongo`, `spring-security-jwt`, `spring-testcontainers`, `observability`, `caching`, `error-handling`.

## Doc index

`infrastructure/docs/GRAPH.md` (synced by `doc-graph-updater`).
