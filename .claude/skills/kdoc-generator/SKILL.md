---
name: KDoc_Generator
description: Generates Kotlin KDoc comments for structural types (classes, interfaces, sealed classes, enums, objects, exceptions). Activates only when user invokes `/kdoc <file_path>`. Should skip method/function bodies and simple refactoring updates.
type: general-purpose
---

## Purpose

Write KDoc to Kotlin structural elements. Targets `class`, `interface`, `sealed class`, `enum`, `object`, and `exception` types in Kotlin source files. Goal is documenting *what* the structure is, not *how* it functions internally.

## Usage (CLI)

Invoke using: `/kdoc <absolute_file_path>`

The skill reads the file at `<absolute_file_path>`, identifies structural declarations, and adds comprehensive KDoc blocks above them. Only structural definitions receive comments. Method signatures/bodies are untouched unless part of a larger structural change.

## Structural Rules
1.  **Target**: `class`, `interface`, `sealed class`, `enum`, `object`, `exception`.
2.  **Avoid**: Methods, properties (unless they define the structure itself), simple refactors where logic changes but type remains identical.
3.  **Style**: Standard Kotlin KDoc format.

## Examples of Scope
*   `@DomainEntity class User`: Target. Needs documentation.
*   `interface IUserRepository`: Target. Needs documentation.
*   `sealed class UserException`: Target. Needs documentation.
*   `fun update(...)`: Skip. Do not add comments to function signatures alone.

## Test Cases (To be developed)
The skill should pass tests covering standard domain/app/infra structures and fail on simple method changes or file non-existence.
