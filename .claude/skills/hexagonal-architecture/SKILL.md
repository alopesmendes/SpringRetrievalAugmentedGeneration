---
name: hexagonal-architecture
description: Expert hexagonal architecture (ports & adapters) advisor for backend systems. Covers layer design, API vs SPI ports, framework independence, module boundaries, tradeoffs, and code review. Trigger when user asks about hexagonal architecture, ports and adapters, layer separation, swapping frameworks, domain purity, output/input ports, driven/driving adapters, Clean Architecture mapping, or structuring a new feature. Also trigger when user adds/modifies domain entities, use cases, repositories, adapters, or questions why something belongs in a specific layer.
---

# Hexagonal Architecture Skill

Ports & Adapters. Domain at center. Infra at edge. Dependency arrow always points inward.

## Layer map

```
Infrastructure → Application → Domain
     (adapters)    (use cases)  (entities/VOs)
```

| Layer             | Allowed deps              | Forbidden                     |
|-------------------|---------------------------|-------------------------------|
| `domain/`         | Kotlin stdlib only        | Everything else               |
| `application/`    | `domain/` + Kotlin stdlib | Spring, Mongo, Jackson, etc.  |
| `infrastructure/` | All three + any framework | Reaching into `domain/` logic |

## Ports

Two kinds:

**Input port (driving / primary)** — what the app offers to the outside world.
- Defined in `application/port/primary/`.
- Interface implemented by use case.
- Inbound adapter (REST, CLI, event consumer) calls through this port.

```kotlin
interface ICreateUserUseCase {
    operator fun invoke(command: CreateUserCommand): Result<UserResult>
}
```

**Output port (driven / secondary)** — what the app needs from infrastructure.
- Defined in `application/port/secondary/`.
- Interface implemented by infra adapter.
- Use case calls through this port. Never touches impl.

```kotlin
interface IUserRepository {
    fun save(user: User): User
    fun findByEmail(email: Email): User?
}
```

## Adapters

**Inbound (primary)** — translate external request → command → call input port.

```
REST request → Controller → dto.toCommand() → ICreateUserUseCase.invoke()
```

**Outbound (secondary)** — implement output port → translate domain ↔ storage model.

```
IUserRepository.save(user) → user.toDocument() → MongoRepository.save()
```

Rule: adapter translates and delegates. Zero domain logic. Zero business decisions.

## Dependency injection

Use cases are plain classes. Infra wires them:

```kotlin
@Configuration
class UserBeanConfig {
    @Bean fun createUserUseCase(repo: IUserRepository, hasher: IPasswordHasherService): ICreateUserUseCase =
        CreateUserUseCase(repo, hasher)
}
```

No `@Service` in application module. Constructor injection everywhere.

## Decision guide

| Question                                       | Answer                                         |
|------------------------------------------------|------------------------------------------------|
| Logic involves a single entity?                | Method on entity                               |
| Logic spans multiple entities or needs a port? | Domain service                                 |
| Orchestration + port calls?                    | Use case                                       |
| HTTP / DB / framework concern?                 | Adapter in infra                               |
| New external dep?                              | Define port in application, implement in infra |

## What goes where — quick checks

**Domain** ✓ if: pure data transform, invariant, business rule that needs no I/O.
**Domain** ✗ if: needs a network call, DB, random, clock, or framework class.

**Application** ✓ if: orchestrates domain + ports, no framework import.
**Application** ✗ if: has `@Service`, `@Repository`, or any Spring annotation.

**Infrastructure** ✓ if: touches Spring, Mongo, HTTP, security, or external service.
**Infrastructure** ✗ if: contains business logic that belongs in domain.

## Common violations and fixes

| Violation                                | Fix                                                    |
|------------------------------------------|--------------------------------------------------------|
| Use case imports `MongoRepository`       | Define output port in application; implement in infra  |
| Controller builds domain entity directly | Controller builds command; use case builds entity      |
| Domain entity has `@Document` annotation | Move Mongo annotation to separate `UserDocument` class |
| Repository method accepts DTO            | Port accepts domain type; adapter converts             |
| `if/else` business logic in controller   | Push to use case or domain                             |

## Consistency checklist

Before adding any class, answer:
1. Which layer owns this responsibility?
2. Does it import anything from a layer it must not depend on?
3. Does the adapter translate (not decide)?
4. Is the port interface defined in `application/`, not `infrastructure/`?
5. Konsist test will catch violation — run `./gradlew unitTest` to confirm.
