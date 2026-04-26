---
name: testing-strategy
description: Testing strategy for this project. Covers which test type belongs in which layer, MockK usage rules, Testcontainers integration tests, Cucumber E2E, backtick naming, and coverage thresholds. Trigger when writing or reviewing tests, deciding between unit/integration/E2E, choosing what to mock, naming test methods, or debugging test failures.
---

# Testing Strategy Skill

Tests live where the code lives. Layer determines test type.

## Layer → test type map

| Layer                      | Test type   | Location                                                        | Needs                   |
|----------------------------|-------------|-----------------------------------------------------------------|-------------------------|
| `domain/`                  | Unit        | `domain/src/test/kotlin/{feature}/{type}/{Class}Test.kt`        | Nothing — pure Kotlin   |
| `application/`             | Unit        | `application/src/test/kotlin/{feature}/useCases/{Class}Test.kt` | MockK for ports         |
| `infrastructure/` mappers  | Unit        | `infrastructure/src/test/kotlin/{feature}/adapter/...`          | Nothing                 |
| `infrastructure/` adapters | Integration | `infrastructure/src/test/kotlin/{feature}/integration/`         | Docker (Testcontainers) |
| Full flow                  | E2E         | `infrastructure/src/test/kotlin/.../e2e/`                       | Full app + Mongo        |

## Commands

```bash
./gradlew unitTest           # domain + application + infra unit
./gradlew integrationTest    # infra only — needs Docker
./gradlew e2eTest            # Cucumber scenarios
./gradlew coverage           # enforces 80% min
./gradlew verifyCoverage

# single test
./gradlew :domain:test --tests "user.entity.UserTest"
./gradlew :application:test --tests "user.useCases.CreateUserUseCaseTest"
./gradlew :infrastructure:test --tests "user.adapter.primary.rest.UserRestMapperTest"
```

## Domain tests — pure unit

No mocks. Domain objects are easy to construct — use real instances.

```kotlin
class UserTest {
    @Test
    fun `given valid data when create then user has generated id`() {
        val user = User.create(Email("a@b.com"), Name("Alice"), PasswordHash("hash"), Age(25))
        assertNotNull(user.id.value)
    }

    @Test
    fun `given empty email when create Email then throws`() {
        assertThrows<IllegalArgumentException> { Email("") }
    }
}
```

## Application tests — mock ports only

Mock output ports (`IUserRepository`, `IPasswordHasherService`, etc.). Use real domain entities.

```kotlin
class CreateUserUseCaseTest {
    private val repo = mockk<IUserRepository>()
    private val hasher = mockk<IPasswordHasherService>()
    private val useCase = CreateUserUseCase(repo, hasher)

    @Test
    fun `given new email when invoke then saves user and returns result`() {
        every { repo.findByEmail(any()) } returns null
        every { hasher.hash(any()) } returns PasswordHash("hashed")
        every { repo.save(any()) } answers { firstArg() }

        val result = useCase(CreateUserCommand(Email("a@b.com"), Name("Alice"), "pw", Age(25)))

        assertTrue(result.isSuccess)
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `given existing email when invoke then returns failure`() {
        val existing = User.create(Email("a@b.com"), Name("Alice"), PasswordHash("h"), Age(25))
        every { repo.findByEmail(any()) } returns existing

        val result = useCase(CreateUserCommand(Email("a@b.com"), Name("Bob"), "pw", Age(30)))

        assertTrue(result.isFailure)
        assertIs<UserException.EmailAlreadyExists>(result.exceptionOrNull())
        verify(exactly = 0) { repo.save(any()) }
    }
}
```

## Infrastructure unit tests — mappers, no Spring

No Spring context needed for mapper tests. Pure function tests.

```kotlin
class UserRestMapperTest {
    @Test
    fun `given valid dto when toCommand then maps all fields`() {
        val dto = CreateUserRequestDto("a@b.com", "Alice", "pw", 25)
        val cmd = dto.toCommand()
        assertEquals("a@b.com", cmd.email.value)
    }
}
```

## Integration tests — Testcontainers

Extend `AbstractMongoIntegrationTest`. Spins real Mongo container. Tests full adapter round-trip.

```kotlin
class UserRepositoryIntegrationTest : AbstractMongoIntegrationTest() {
    @Autowired lateinit var repo: UserRepository

    @Test
    fun `given saved user when findByEmail then returns user`() {
        val user = User.create(Email("a@b.com"), Name("Alice"), PasswordHash("h"), Age(25))
        repo.save(user)
        val found = repo.findByEmail(Email("a@b.com"))
        assertNotNull(found)
        assertEquals(user.email, found!!.email)
    }
}
```

## E2E tests — Cucumber

Scenarios in `src/test/resources/features/`. Step defs in `e2e/` package.

```gherkin
Feature: Create user

  Scenario: successful creation
    Given no user exists with email "a@b.com"
    When I POST /users with valid payload
    Then response status is 201
    And response body contains id
```

## Naming — backtick given/when/then

```kotlin
fun `given {context} when {action} then {expectation}`()
```

All test methods use backtick format. No camelCase for test names.

## MockK rules

- Mock only interfaces (ports). Never mock domain entities or value objects.
- Use `every { } returns` for happy path, `throws` for failures.
- Use `verify(exactly = N)` to assert interaction count — not just `verify { }`.
- `relaxed = true` only when interaction count is irrelevant and you're testing output only.

```kotlin
// bad — mocking a domain object
val user = mockk<User>()

// good — construct real domain object
val user = User.create(Email("a@b.com"), Name("Alice"), PasswordHash("h"), Age(25))
```

## Coverage

80% minimum enforced by Kover (`./gradlew verifyCoverage`). Coverage gates per module.

Priority order: domain invariants > use case paths > adapter mappings > config beans.

Don't write tests purely for coverage. Every test should assert a real behavior or invariant.

## What NOT to test

- Private methods — test through public API.
- Framework wiring — Spring context loads are integration tests, not unit.
- Getters/setters on data classes — Kotlin generates these; no value in testing.
- MockK setup itself — don't assert the mock was configured, assert the outcome.
