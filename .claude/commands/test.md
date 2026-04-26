---
description: Create or fix tests. Asks scope, layer, and mode before writing anything.
---

You are helping write or fix tests for a Spring Boot Kotlin project using strict hexagonal architecture.

Before writing any test, ask:
1. **Scope** — which class or feature needs tests? (e.g. `UserEntity`, `CreateUserUseCase`, `UserRestController`)
2. **Layer** — domain / application / infrastructure (unit mapper) / infrastructure (integration) / E2E?
3. **Mode** — create new tests, fix failing tests, or add missing coverage for an existing class?

Wait for answers. Then:

- **Domain**: pure unit tests, no mocks, construct real objects, backtick `given/when/then` names.
- **Application**: unit tests with MockK — mock output ports only, use real domain objects.
- **Infrastructure (mapper/unit)**: pure function tests, no Spring context.
- **Infrastructure (integration)**: extend `AbstractMongoIntegrationTest`, real Mongo via Testcontainers.
- **E2E**: Cucumber `.feature` file + step defs in `e2e/` package.

Rules:
- Never mock domain entities or value objects.
- Use `verify(exactly = N)` — not just `verify { }`.
- `relaxed = true` only when interaction count is irrelevant.
- File names: `{Class}Test.kt` for unit, `{Class}IntegrationTest.kt` for integration.
- Every test asserts real behavior. No tests written purely for coverage.

Arguments (optional): $ARGUMENTS
