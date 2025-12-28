# Architecture Guide - Image RAG Application

## Overview

This project follows **Hexagonal Architecture** (Ports & Adapters) to ensure framework independence. The domain logic remains pure Kotlin with no Spring dependencies.

---

## Hexagonal Architecture Principles

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           INFRASTRUCTURE                                 │
│  ┌────────────────────┐                      ┌────────────────────────┐  │
│  │   Primary Adapters │                      │   Secondary Adapters   │  │
│  │   (REST, CLI, UI)  │                      │   (DB, AI, External)   │  │
│  └─────────┬──────────┘                      └───────────┬────────────┘  │
│            │                                             │               │
│            ▼                                             ▲               │
│  ┌─────────────────┐                         ┌───────────────────────┐   │
│  │  Input Ports    │                         │    Output Ports       │   │
│  │  (Use Cases)    │                         │    (Repositories)     │   │
│  └─────────┬───────┘                         └───────────▲───────────┘   │
│            │      ┌─────────────────────┐                │               │
│            │      │     APPLICATION     │                │               │
│            └─────►│                     │────────────────┘               │
│                   │   (Orchestration)   │                                │
│                   └──────────┬──────────┘                                │
│                              │                                           │
│                              ▼                                           │
│                   ┌─────────────────────┐                                │
│                   │       DOMAIN        │                                │
│                   │                     │                                │
│                   │  Entities, Value    │                                │
│                   │  Objects, Services  │                                │
│                   └─────────────────────┘                                │
└──────────────────────────────────────────────────────────────────────────┘
```

**Flow Summary:**
- **Inbound**: REST Controller → Input Port → Use Case → Domain → Output Port → Repository
- **Outbound**: Domain defines interfaces (ports), Infrastructure implements them (adapters)

---

## Key Design Decisions

1. **Use Cases as Single Responsibility Units**
    - Each use case handles one specific business operation
    - Easier to test and maintain

2. **Mappers at Boundaries**
    - REST DTOs ↔ Application DTOs ↔ Domain Objects
    - Persistence Documents ↔ Domain Objects
    - Prevents leaking infrastructure concerns into domain

3. **Domain Exceptions**
    - Domain throws domain-specific exceptions
    - Infrastructure layer handles translation to HTTP status codes

4. **No Anemic Domain**
    - Business logic lives in domain entities and services
    - Use cases orchestrate but don't contain business rules

5. **Framework Independence via Custom Annotations**
    - Domain uses its own annotations (`@DomainEntity`, `@ValueObject`)
    - Infrastructure configuration maps these to Spring beans without domain awareness

---

## Layer Structure & Dependency Rules

### Dependency Direction

```
Infrastructure ──────► Application ──────► Domain
```

| Layer          | Can Depend On       | Cannot Depend On            |
|----------------|---------------------|-----------------------------|
| Domain         | Kotlin stdlib only  | Application, Infrastructure |
| Application    | Domain              | Infrastructure              |
| Infrastructure | Application, Domain | -                           |

**Enforcement:** Use ArchUnit tests to verify dependency rules. Each layer is a separate Gradle module.

---

### Layer Details (Prose Format)

#### 1. Domain Layer (Core)
**Location:** `domain/`

The innermost layer containing pure business logic with **zero framework dependencies**.

**Contains:**
- **Entities** - Core business objects with identity
- **Value Objects** - Immutable objects without identity
- **Domain Services** - Business logic that doesn't belong to a single entity
- **Domain Exceptions** - Business-specific exceptions
- **Domain Annotations** - Custom annotations to replace Spring annotations

**Rules:**
- NO Spring imports
- NO infrastructure concerns
- Only depends on Kotlin stdlib and domain-specific libraries

#### 2. Application Layer
**Location:** `application/`

Orchestrates use cases and defines ports.

**Contains:**
- **Input Ports** - Interfaces defining use cases
- **Output Ports** - Interfaces for external dependencies
- **Use Case Implementations** - Concrete implementations of input ports
- **DTOs** - Data Transfer Objects for port communication

**Rules:**
- Depends only on Domain layer
- Defines interfaces (ports) that infrastructure implements
- NO framework dependencies

#### 3. Infrastructure Layer
**Location:** `infrastructure/`

Contains all framework-specific implementations (adapters).

**Contains:**
- **Inbound Adapters** (Driving) - REST Controllers, API DTOs, OpenAPI config
- **Outbound Adapters** (Driven) - MongoDB repositories, Spring AI adapter, Security
- **Configuration** - Spring Boot config, Bean definitions

**Rules:**
- Implements ports defined in Application layer
- Contains ALL Spring/framework dependencies
- Maps between infrastructure DTOs and domain objects

**Domain-Spring Bridge:** The `config/` package contains `@Configuration` classes that scan for domain annotations and register them as Spring beans. The domain remains unaware of Spring—configuration acts as the glue.

---

## Port Definitions

### Input Ports (Driving/Primary)
Define **what** the application can do - the use cases.

```kotlin
interface IExampleUseCase {
    operator fun invoke(command: ExampleCommand): Result<ExampleResult>
}
```

### Output Ports (Driven/Secondary)
Define **what** the application needs from external systems.

```kotlin
interface IExampleRepository {
    fun save(entity: ExampleEntity): ExampleEntity
    fun findById(id: ExampleId): ExampleEntity?
    fun delete(id: ExampleId)
}

interface IExternalServicePort {
    fun process(data: ByteArray): ProcessResult
}
```

---

## Folder Organization

```
src/
├── main/
│   └── kotlin/
│       └── com/example/projectname/
│           │
│           ├── domain/
│           │   ├── model/
│           │   │   ├── entity/
│           │   │   └── valueobject/
│           │   ├── service/
│           │   ├── exception/
│           │   └── annotation/
│           │
│           ├── application/
│           │   ├── port/
│           │   │   ├── input/
│           │   │   └── output/
│           │   ├── usecase/
│           │   └── dto/
│           │
│           └── infrastructure/
│               ├── adapter/
│               │   ├── primary/
│               │   │   ├── rest/
│               │   │   │   ├── dto/
│               │   │   │   └── mapper/
│               │   │   └── security/
│               │   └── secondary/
│               │       ├── persistence/
│               │       │   ├── document/
│               │       │   └── mapper/
│               │       └── ai/
│               │           └── mapper/
│               └── config/
```

---

### Example: User Feature Structure

```
domain/
├── model/
│   ├── entity/
│   │   └── User.kt                    # User entity with business logic
│   └── valueobject/
│       ├── UserId.kt                  # Typed identifier
│       ├── Email.kt                   # Email with validation
│       └── PasswordHash.kt            # Password hash wrapper
├── exception/
│   └── UserException.kt               # UserNotFoundException, etc.
└── service/
    └── PasswordService.kt             # Domain password hashing logic

application/
├── port/
│   ├── input/
│   │   ├── ICreateUserUseCase.kt
│   │   ├── IGetUserUseCase.kt
│   │   └── IAuthenticateUserUseCase.kt
│   └── output/
│       ├── IUserRepository.kt
│       └── IPasswordHasher.kt
├── usecase/
│   ├── CreateUserUseCase.kt
│   ├── GetUserUseCase.kt
│   └── AuthenticateUserUseCase.kt
└── dto/
    ├── CreateUserCommand.kt
    ├── AuthenticateCommand.kt
    └── UserResult.kt

infrastructure/
├── adapter/
│   ├── primary/
│   │   └── rest/
│   │       ├── UserController.kt
│   │       ├── dto/
│   │       │   ├── CreateUserRequest.kt
│   │       │   └── UserResponse.kt
│   │       └── mapper/
│   │           └── UserRestMapper.kt
│   └── secondary/
│       ├── persistence/
│       │   ├── MongoUserRepository.kt  # Implements IUserRepository
│       │   ├── SpringDataUserRepository.kt
│       │   ├── document/
│       │   │   └── UserDocument.kt
│       │   └── mapper/
│       │       └── UserPersistenceMapper.kt
│       └── security/
│           └── BCryptPasswordHasher.kt # Implements IPasswordHasher
└── config/
    └── UserBeanConfig.kt              # Wires use cases as Spring beans
```

---

## Framework Independence Strategy

### Domain Annotations
Create custom annotations to mark domain concepts without Spring:

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DomainEntity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueObject
```

### Separate Gradle Modules (Recommended)
```
project/
├── domain/          # Pure Kotlin, no Spring
│   └── build.gradle.kts
├── application/     # Pure Kotlin, no Spring
│   └── build.gradle.kts
└── infrastructure/  # Spring Boot, MongoDB, Spring AI
    └── build.gradle.kts
```
