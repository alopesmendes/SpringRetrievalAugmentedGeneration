# CLAUDE.md

Root context. Module specifics live in `{domain,application,infrastructure}/CLAUDE.md`.

## Project

Spring Boot Kotlin app. Image RAG (Retrieval Augmented Generation) — extract + store info from images via Spring AI. Three Gradle submodules. Strict hexagonal.

Stack today: Kotlin 2.2, Spring Boot 4, Spring AI 1.1, MongoDB, Testcontainers, Cucumber. Stack swappable — domain + application are framework-free.

## Collaborator stance

You are a teammate, not a code vending machine.

- Ask before writing. Doubt → ask user, never guess.
- Challenge bad ideas. If a request violates hexagonal or smells wrong, push back with reason.
- Permission required for every file write. No autonomy on irreversible ops.
- Prefer asking 2 short questions over silently picking a tradeoff.

## Architecture

```
Infrastructure → Application → Domain
```

- `domain/` — pure Kotlin. Entities, value objects, domain services, domain exceptions. **Zero Spring imports.**
- `application/` — pure Kotlin. Input ports (use case interfaces), output ports (repo/service interfaces), use case impls, commands, results. **Zero Spring imports.**
- `infrastructure/` — Spring + framework code. Inbound adapters (REST, security), outbound adapters (Mongo, BCrypt), config beans. Integration + E2E tests live here.

Compliance enforced at build: Konsist tests in each module's `architecture/` test pkg.

## Commands

```bash
./setup.sh                  # install git hooks
./gradlew bootRun           # APP_ENVIRONMENT=development required
./gradlew lint              # ktlint + detekt
./gradlew format            # ktlint --auto-fix
./gradlew unitTest          # all modules
./gradlew integrationTest   # infra only — needs Docker
./gradlew e2eTest           # Cucumber
./gradlew coverage          # 80% min enforced
./gradlew verifyCoverage
./gradlew securityScan      # Trivy
```

Single test:
```bash
./gradlew :domain:test --tests "user.entity.UserTest"
./gradlew :application:test --tests "user.useCases.CreateUserUseCaseTest"
./gradlew :infrastructure:test --tests "user.adapter.primary.rest.UserRestMapperTest"
```

## Naming (project-wide)

| Thing        | Convention                                     | Example                                       |
|--------------|------------------------------------------------|-----------------------------------------------|
| Interfaces   | `I` prefix                                     | `IUserRepository`, `ICreateUserUseCase`       |
| Use cases    | `{Action}{Entity}UseCase`                      | `CreateUserUseCase`                           |
| Mappers      | `{Layer}{Entity}Mapper`                        | `UserPersistenceMapper`, `UserRestMapper`     |
| DTOs         | `{Entity}{Purpose}Dto`                         | `CreateUserRequestDto`, `UserResponseDto`     |
| Mongo docs   | `{Entity}Document`                             | `UserDocument`                                |
| Test methods | backtick `given/when/then`                     | `` `given valid email when create then ok` `` |
| Test files   | `{Class}Test.kt` / `{Class}IntegrationTest.kt` |                                               |

## Cross-cutting rules

- Domain pure. Zero framework imports. Validation in `init`. Factory `create()` for invariants.
- Errors typed. `Result<T>` from domain/application. Infra folds to HTTP. Never swallow.
- Mappers = extension functions at layer boundary. Never inside use cases.
- One reason to change per class. No god services.
- No premature abstraction. Rule of three.
- Tests follow layer: unit (domain/app), integration (infra adapters), E2E (Cucumber).

## Skills available (auto-load on match)

Tech-agnostic: `hexagonal-architecture`, `kotlin-style`, `clean-code`, `error-handling`, `testing-strategy`, `documentation`, `domain-modeling`, `api-design`.

Spring-specific (infra layer): `spring-boot-config`, `spring-rest-mvc`, `spring-data-mongo`, `spring-security-jwt`, `spring-testcontainers`, `observability`, `caching`.

Roadmap (deferred): `event-sourcing`, `notification-messaging`, `kotlin-ktor-migration`.

## Commands available

- `/test` — create or fix tests (asks scope, layer, mode)
- `/domain` — design domain logic (entity, VO, service, exception)
- `/application` — design use cases, ports, DTOs
- `/infrastructure` — design adapters (REST, persistence, security)
- `/feature` — orchestrate `/domain` → `/application` → `/infrastructure`
- `/documentation` — write or update docs

## Agents available

- `hexagonal-arch-planner` — design feature architecture (Mermaid + reasoning, no code)
- `hexagonal-arch-scaffolder` — empty file scaffolds from planner output
- `doc-graph-updater` — sync `{module}/docs/GRAPH.md`
- `docs-lookup` — answer questions from docs
- `code-reviewer` — Kotlin clean-code + hex layer audit
- `security-engineer` — JWT, filter chain, secrets, deps audit
- `test-automator` — gen unit/integration/E2E suite respecting layer contract

## Git

Branch: `prefix/issue-number-short-description` (e.g. `feat/123-add-user-auth`).
Hook `prepare-commit-msg` prepends `prefix(#issue):` from branch. Write body only.
Prefixes: `feat`, `fix`, `hotfix`, `chore`, `docs`, `refactor`. Gitmoji shortcodes supported.

## Pointers

- Tracker: `docs/CLAUDE_PROGRESS.md`
- API contracts ref: `docs/API_CONTRACTS.md`
- Test strategy ref: `docs/TEST_STRATEGY.md`
- Module rules: `{module}/CLAUDE.md`
- Module doc index: `{module}/docs/GRAPH.md`
