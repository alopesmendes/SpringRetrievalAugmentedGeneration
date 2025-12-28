# SpringRetrievalAugmentedGeneration

[![codecov](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration/graph/badge.svg?token=ZOB7G5BEKR)](https://codecov.io/github/alopesmendes/SpringRetrievalAugmentedGeneration)
[![CI](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Ci.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Ci.yml)
[![Security](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Security.yml/badge.svg)](https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration/actions/workflows/Security.yml)

A Spring Boot Kotlin project for Image Retrieval Augmented Generation (RAG) - extracting and storing information from images using Spring AI.

## 🚀 Quick Start

### Prerequisites

- JDK 21+
- Docker (for MongoDB via Testcontainers)
- Gradle 8.14+

### Setup & Run

```bash
# Clone and setup
git clone https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration.git
cd SpringRetrievalAugmentedGeneration
./setup.sh

# Run locally
export APP_ENVIRONMENT=development
./gradlew bootRun
```

## 📋 Available Commands

| Category     | Command                          | Description                                      |
|--------------|----------------------------------|--------------------------------------------------|
| **Linting**  | `./gradlew lint`                 | Run ktlint + detekt                              |
|              | `./gradlew format`               | Auto-format code with ktlint                     |
| **Testing**  | `./gradlew unitTest`             | Run unit tests for all modules                   |
|              | `./gradlew integrationTest`      | Run integration tests (infrastructure)           |
|              | `./gradlew e2eTest`              | Run E2E tests with Cucumber                      |
|              | `./gradlew assembleTestResults`  | Collect all test results into unified directory  |
| **Coverage** | `./gradlew coverage`             | Generate coverage reports (skips tests if exist) |
|              | `./gradlew verifyCoverage`       | Verify 80% minimum threshold                     |
|              | `./gradlew printCoverageSummary` | Print coverage summary to console                |
| **Security** | `./gradlew securityScan`         | Run Trivy vulnerability scan                     |
|              | `./gradlew verifySecurity`       | Fail if CRITICAL/HIGH vulnerabilities found      |
|              | `./gradlew printSecuritySummary` | Print security scan summary                      |
| **Build**    | `./gradlew build`                | Build all modules                                |
|              | `./gradlew bootRun`              | Run the application locally                      |

## 🚢 Deployment

The project is hosted on **Render** using Docker containers. Configuration files:

- `render.yaml` - Render Blueprint (Infrastructure as Code)
- `Dockerfile` - Multi-stage build for optimized production image

### Deploy via GitHub Actions

1. Go to **GitHub → Actions → CI workflow**
2. Click **Run workflow**
3. Check **"Skip deployment"** to run CI only, or leave unchecked to deploy

### Automatic Deployment

Deployments trigger automatically on:

- **Push to `master`** → Deploys to `prod`
- **Push to `staging`** → Deploys to `staging`
- **Push to `develop`** → Deploys to `test`

> ⚠️ **Note:** Direct pushes to protected branches trigger immediate deployment. Use Pull Requests for code review before merging.

## 📁 Project Structure

```
├── domain/           # Pure Kotlin business logic (no framework dependencies)
├── application/      # Use cases and ports (no framework dependencies)
├── infrastructure/   # Spring Boot adapters and configurations
├── docs/             # Documentation (auto-synced to GitHub Wiki)
├── config/           # Detekt and linting configuration
└── .github/workflows # CI/CD pipelines
```

## 📚 Documentation

Full documentation is available in the [GitHub Wiki](https://docs.github.com/fr/communities/documenting-your-project-with-wikis/about-wikis).

| Document                                   | Purpose                                           |
|--------------------------------------------|---------------------------------------------------|
| [Architecture](docs/ARCHITECTURE.md)       | Hexagonal architecture guide and layer boundaries |
| [API Contracts](docs/API_CONTRACTS.md)     | REST controller conventions and OpenAPI patterns  |
| [Git Conventions](docs/GIT_CONVENTIONS.md) | Commit message format and branch naming           |
| [GitHub Setup](docs/GITHUB_SETUP.md)       | GitHub environments, secrets, and Render setup    |
| [Rules](docs/RULES.md)                     | Naming conventions and coding rules               |
| [Test Strategy](docs/TEST_STRATEGY.md)     | Testing pyramid and test organization             |

> 📝 Documentation in `docs/` is automatically synced to the GitHub Wiki on push to protected branches.

## 📊 Code Quality

| Metric          | Threshold | Tool    |
|-----------------|-----------|---------|
| Line Coverage   | 80%       | Kover   |
| Static Analysis | Enabled   | Detekt  |
| Code Style      | Enforced  | ktlint  |
| Security Scan   | Enabled   | Trivy   |

## 📜 License

This project is licensed under the MIT License.
