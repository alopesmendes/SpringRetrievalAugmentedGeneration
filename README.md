# SpringRetrievalAugmentedGeneration

[![codecov](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration/graph/badge.svg?token=ZOB7G5BEKR)](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration)[![Coverage](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Coverage.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Coverage.yml)
[![Test](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Test.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Test.yml)
[![Build](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Build.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Build.yml)
[![Lint](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Lint.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Lint.yml)

A Spring Boot Kotlin project for Image Retrieval Augmented Generation (RAG) - extracting and storing information from images using Spring AI.

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters) with three distinct modules:

```
┌─────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE                         │
│  Spring Boot • Spring AI • MongoDB • Spring Security        │
│                          ↓                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    APPLICATION                      │    │
│  │            Use Cases • Ports • DTOs                 │    │
│  │                          ↓                          │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │                   DOMAIN                    │    │    │
│  │  │   Entities • Value Objects • Domain Services│    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## 📊 Code Quality

| Metric          | Threshold | Status                                                                                                                                                                                                            |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Line Coverage   | 80%       | [![codecov](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration/graph/badge.svg?token=ZOB7G5BEKR)](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration)                          |
| Static Analysis | detekt    | [![Lint](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Lint.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Lint.yml) |
| Code Style      | ktlint    | Enforced                                                                                                                                                                                                          |

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- Docker (for MongoDB via Testcontainers)
- Gradle 8.14+

### Setup

```bash
# Clone the repository
git clone https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration.git
cd SpringRetrievalAugmentedGeneration

# Run initial setup (installs pre-commit hooks)
./setup.sh

# Build the project
./gradlew build

# Run tests
./gradlew test
```

### Available Commands

```bash
# Linting
./gradlew lint           # Run ktlint + detekt
./gradlew format         # Auto-format code

# Testing
./gradlew test           # Unit tests
./gradlew integrationTest # Integration tests
./gradlew e2eTest        # E2E tests (Cucumber)

# Coverage
./gradlew koverHtmlReport  # Generate HTML coverage report
./gradlew koverVerify      # Verify 80% minimum coverage
```

## 📁 Project Structure

```
├── domain/           # Pure Kotlin business logic (no framework dependencies)
├── application/      # Use cases and ports (no framework dependencies)
├── infrastructure/   # Spring Boot adapters and configurations
├── config/
│   └── detekt/       # Static analysis configuration
└── .github/
    └── workflows/    # CI/CD pipelines
```

## 🧪 Testing Strategy

| Layer          | Test Type      | Focus                           |
|----------------|----------------|---------------------------------|
| Domain         | Unit           | Business logic, validation      |
| Application    | Unit           | Use case orchestration          |
| Infrastructure | Integration    | Adapters, DB, external services |
| Full Stack     | E2E (Cucumber) | Complete workflows              |

## 📜 License

This project is licensed under the MIT License.
