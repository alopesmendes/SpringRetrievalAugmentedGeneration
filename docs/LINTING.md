# Code Quality & Linting Setup

This project uses **ktlint** and **detekt** for code quality enforcement with **pre-commit** hooks.

## Tools Overview

| Tool           | Purpose                 | Version |
|----------------|-------------------------|---------|
| **ktlint**     | Code style & formatting | 1.5.0   |
| **detekt**     | Static code analysis    | 1.23.8  |
| **pre-commit** | Git hooks framework     | 5.0.0   |

## Quick Start

```bash
# Initial setup (run once after cloning)
./setup.sh

# Or manually:
pip install pre-commit
pre-commit install
```

## Available Commands

### Using Make

```bash
make lint           # Run all linters (ktlint + detekt)
make format         # Auto-format code with ktlint
make detekt         # Run detekt only
make check          # Run linters + tests
make test           # Run tests only
make build          # Build project
make clean          # Clean build artifacts
make baseline       # Generate detekt baseline
make install-hooks  # Install pre-commit hooks
```

### Using Gradle

```bash
# Ktlint
./gradlew ktlintCheck        # Check code style
./gradlew ktlintFormat       # Auto-fix style issues

# Detekt
./gradlew detekt             # Run static analysis
./gradlew detektGenerateBaseline  # Generate baseline

# Combined
./gradlew lint               # Run both ktlint and detekt
./gradlew format             # Format code
```

### Using Pre-commit

```bash
pre-commit run --all-files   # Run all hooks on all files
pre-commit run ktlint-check  # Run ktlint only
pre-commit run detekt        # Run detekt only

# Manual format (not in automatic hooks)
pre-commit run ktlint-format --hook-stage manual
```

## Configuration Files

```
project/
├── .pre-commit-config.yaml   # Pre-commit hooks configuration
├── .editorconfig             # Editor & ktlint configuration
├── config/
│   └── detekt/
│       ├── detekt.yml        # Detekt rules configuration
│       └── baseline.xml      # Detekt baseline (generated)
└── tools/
    └── scripts/
        ├── ktlint-check.sh   # Ktlint check script
        ├── ktlint-format.sh  # Ktlint format script
        └── detekt-check.sh   # Detekt check script
```

## Pre-commit Hooks

The following hooks run automatically on each commit:

| Hook                      | Description                   | Runs On         |
|---------------------------|-------------------------------|-----------------|
| `trailing-whitespace`     | Remove trailing whitespace    | All files       |
| `end-of-file-fixer`       | Ensure files end with newline | All files       |
| `check-yaml`              | Validate YAML syntax          | `.yml`, `.yaml` |
| `check-added-large-files` | Block large files (>1MB)      | All files       |
| `check-merge-conflict`    | Check for merge conflicts     | All files       |
| `detect-private-key`      | Prevent committing secrets    | All files       |
| `ktlint-check`            | Check Kotlin code style       | `.kt`, `.kts`   |
| `detekt`                  | Static code analysis          | `.kt`           |

## Ktlint Rules

Ktlint follows the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html) with some additions:

- Max line length: **120 characters**
- Indent: **4 spaces**
- No wildcard imports
- Trailing commas enabled
- Multi-line function signatures for 3+ parameters

Configuration is in `.editorconfig`.

## Detekt Rules

Detekt performs comprehensive static analysis including:

### Complexity
- Cyclomatic complexity threshold: 15
- Cognitive complexity threshold: 15
- Max function length: 60 lines
- Max class size: 600 lines
- Max parameters: 6 (functions), 7 (constructors)

### Naming
- Classes: `PascalCase`
- Functions/Variables: `camelCase`
- Constants: `SCREAMING_SNAKE_CASE`
- Boolean properties: must start with `is`, `has`, `are`, `can`, `should`, `may`, `will`

### Style
- No magic numbers (except -1, 0, 1, 2)
- Max 3 return statements per function
- Braces required on multi-line if/else
- Data classes should be immutable

### Potential Bugs
- No unsafe casts
- No unnecessary null checks
- No platform types

Full configuration is in `config/detekt/detekt.yml`.

## Handling Issues

### Suppressing False Positives

For ktlint:
```kotlin
@Suppress("ktlint:standard:no-wildcard-imports")
import java.util.*
```

For detekt:
```kotlin
@Suppress("MagicNumber")
val timeout = 5000
```

### Creating a Baseline

If you have many existing issues, create a baseline to ignore them:

```bash
./gradlew detektGenerateBaseline
```

This creates `config/detekt/baseline.xml`. New issues will still be reported.

### Disabling Hooks Temporarily

For a single commit (not recommended):
```bash
git commit --no-verify -m "message"
```

## IDE Integration

### IntelliJ IDEA

1. Install the **Ktlint** plugin
2. Install the **Detekt** plugin
3. Configure both to use project configuration files

### VS Code

1. Install **Kotlin** extension
2. Configure editor to respect `.editorconfig`

## CI/CD Integration

The same checks should run in your CI pipeline:

```yaml
# GitHub Actions example
- name: Lint
  run: ./gradlew lint

- name: Test
  run: ./gradlew test
```

## Troubleshooting

### "pre-commit not found"

```bash
pip install pre-commit
# or
brew install pre-commit
```

### "ktlint failed" after format

Re-stage the formatted files:
```bash
./gradlew ktlintFormat
git add -u
git commit
```

### Detekt reports too many issues

Generate a baseline:
```bash
./gradlew detektGenerateBaseline
```

### Scripts not executable

```bash
chmod +x tools/scripts/*.sh
```
