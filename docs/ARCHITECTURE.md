# Architecture Guide - Image RAG Application

## Overview

This project follows **Hexagonal Architecture** (Ports & Adapters) to ensure framework independence. The domain logic remains pure Kotlin with no Spring dependencies.

---

## Hexagonal Architecture Principles

```
                    ┌─────────────────────────────────────────┐
                    │              INFRASTRUCTURE             │
                    │  ┌─────────────┐    ┌────────────────┐  │
                    │  │ Spring MVC  │    │   Spring AI    │  │
                    │  │  (REST API) │    │   (Adapter)    │  │
                    │  └──────┬──────┘    └───────┬────────┘  │
                    │         │                   │           │
                    │         ▼                   ▼           │
                    │  ┌─────────────────────────────────┐    │
                    │  │         INPUT PORTS             │    │
                    │  │        (Use Cases)              │    │
                    │  └──────────────┬──────────────────┘    │
                    │                 │                       │
                    │                 ▼                       │
                    │  ┌─────────────────────────────────┐    │
                    │  │           DOMAIN                │    │
                    │  │  (Entities, Value Objects,      │    │
                    │  │   Domain Services, Rules)       │    │
                    │  └──────────────┬──────────────────┘    │
                    │                 │                       │
                    │                 ▼                       │
                    │  ┌─────────────────────────────────┐    │
                    │  │        OUTPUT PORTS             │    │
                    │  │    (Repository Interfaces)      │    │
                    │  └──────────────┬──────────────────┘    │
                    │                 │                       │
                    │                 ▼                       │
                    │  ┌─────────────┐    ┌────────────────┐  │
                    │  │  MongoDB    │    │ Spring Security│  │
                    │  │  (Adapter)  │    │   (Adapter)    │  │
                    │  └─────────────┘    └────────────────┘  │
                    └─────────────────────────────────────────┘
```

---

## Layer Structure

### 1. Domain Layer (Core)
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

---

### 2. Application Layer
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

---

### 3. Infrastructure Layer
**Location:** `infrastructure/`

Contains all framework-specific implementations (adapters).

**Contains:**
- **Inbound Adapters** (Driving)
  - REST Controllers (Spring MVC)
  - API DTOs and mappers
  - OpenAPI/Swagger configuration

- **Outbound Adapters** (Driven)
  - MongoDB Repository implementations
  - Spring AI adapter for image analysis
  - Spring Security configuration

- **Configuration**
  - Spring Boot configuration
  - Bean definitions
  - External service configurations

**Rules:**
- Implements ports defined in Application layer
- Contains ALL Spring/framework dependencies
- Maps between infrastructure DTOs and domain objects

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
│               │   ├── inbound/
│               │   │   ├── rest/
│               │   │   │   ├── dto/
│               │   │   │   └── mapper/
│               │   │   └── security/
│               │   └── outbound/
│               │       ├── persistence/
│               │       │   ├── document/
│               │       │   └── mapper/
│               │       └── ai/
│               │           └── mapper/
│               └── config/
│
└── test/
    └── kotlin/
        └── com/example/projectname/
            ├── domain/
            ├── application/
            └── infrastructure/
                ├── adapter/
                │   ├── inbound/
                │   └── outbound/
                └── e2e/
```

---

## Dependency Rules

```
┌─────────────────────────────────────────────────────────┐
│                    DEPENDENCY DIRECTION                 │
│                                                         │
│   Infrastructure ──────► Application ──────► Domain     │
│                                                         │
│   (Outer layers depend on inner layers, never reverse)  │
└─────────────────────────────────────────────────────────┘
```

| Layer          | Can Depend On       | Cannot Depend On            |
|----------------|---------------------|-----------------------------|
| Domain         | Kotlin stdlib only  | Application, Infrastructure |
| Application    | Domain              | Infrastructure              |
| Infrastructure | Application, Domain | -                           |

**Enforcement:**
- Use ArchUnit tests to verify dependency rules
- Domain module should be a separate Gradle module with no Spring dependencies

---

## Port Definitions

### Input Ports (Driving/Primary)
Define **what** the application can do - the use cases.

```kotlin
interface IExampleUseCase {
    fun execute(command: ExampleCommand): ExampleResult
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

## Adapter Mapping

| Component           | Type              | Implements Port         | Description                        |
|---------------------|-------------------|-------------------------|------------------------------------|
| REST Controller     | Inbound Adapter   | Uses Input Ports        | REST API endpoints                 |
| Persistence Adapter | Outbound Adapter  | IRepository             | Database persistence               |
| AI Adapter          | Outbound Adapter  | IExternalServicePort    | External AI service                |
| Security Adapter    | Inbound Adapter   | -                       | Authentication/Authorization       |

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

---

## Testing Strategy by Layer

| Layer          | Test Type         | Focus                           |
|----------------|-------------------|---------------------------------|
| Domain         | Unit Tests        | Business logic, validation      |
| Application    | Unit Tests        | Use case orchestration          |
| Infrastructure | Integration Tests | Adapters, DB, external services |
| Full Stack     | E2E Tests         | Complete workflows              |
