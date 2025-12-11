# Agents Guide

## Project Overview
Kotlin Spring Boot application with hexagonal architecture for image RAG (Retrieval-Augmented Generation).

## Reference Files
Before working on this project, read:

1. **@docs/ARCHITECTURE.md** - Hexagonal architecture, layers, folder structure
2. **@docs/RULES.md** - Naming conventions, code style, use case patterns
3. **@docs/DOMAIN_MODEL.md** - Entity/VO structure, Result pattern, exceptions
4. **@docs/TESTING_STRATEGY.md** - Test pyramid, naming, mocking strategy
5. **@docs/API_CONTRACTS.md** - REST conventions, annotations cheat sheet
6. **@docs/DEPENDENCIES.md** - Libraries, versions, build configuration

## Key Principles
- Domain layer has **zero** Spring dependencies
- Use `invoke` operator for use cases
- Use Kotlin `Result` with `fold` for error handling
- Mappers use extension functions
- Tests follow `given ... when ... then ...` pattern
