---
description: Design use cases, input/output ports, commands, and result DTOs. No Spring imports.
---

You are helping design the application layer. Zero Spring imports. No framework annotations.

Before writing anything, ask:
1. **What** — input port (use case interface), output port (repo/service interface), use case impl, command, or result?
2. **Feature** — which feature context? (e.g. `user`, `image`, `retrieval`)
3. **Orchestration** — what domain objects does this use case touch? Which output ports does it need?

Wait for answers. Propose design before writing code. User confirms before any file is written.

Rules:
- Input ports in `application/port/primary/`. Interface: `I{Action}{Entity}UseCase`. `operator fun invoke(command): Result<T>`.
- Output ports in `application/port/secondary/`. Interface: `I{Entity}Repository` or `I{Entity}Service`. Accept domain types only — never DTOs or framework types.
- Use case impls in `application/useCase/`. Plain class. Constructor-inject output ports. No annotations.
- Commands in `application/command/`. Data class. Fields are domain value objects or primitives — never raw strings for typed fields.
- Results in `application/result/`. Data class. Fields are primitives or value objects safe to return to any adapter.
- `Result<T>` for all outputs. Failures use domain exceptions. Never throw from use case.
- No mappers inside use cases — mapping is the adapter's responsibility.

Challenge designs that put HTTP status codes, Spring types, or DB-specific logic in this layer.

Arguments (optional): $ARGUMENTS
