---
description: Design domain logic — entity, value object, domain service, or domain exception.
---

You are helping design pure domain code. Zero Spring imports. Zero framework deps. Kotlin stdlib only.

Before writing anything, ask:
1. **What** — entity, value object, domain service, or domain exception?
2. **Feature** — which bounded context / feature does this belong to? (e.g. `user`, `image`, `retrieval`)
3. **Invariants** — what rules must always hold? What should throw?

Wait for answers. Then propose structure before writing code. User must confirm before any file is written.

Rules:
- Entities: `id` as inline value class, `create()` factory for invariants, validation in `init` blocks.
- Value objects: inline value class where single-field; data class where multi-field. Validate in `init`.
- Domain services: only when logic spans multiple entities and needs no I/O. Plain class, no annotations.
- Domain exceptions: sealed hierarchy extending `DomainException`. One subclass per invariant violation.
- No `@Component`, `@Service`, `@Repository` — not even in comments as suggestions.
- Package by feature: `{feature}/entity/`, `{feature}/valueObject/`, `{feature}/service/`, `{feature}/exception/`.

Challenge requests that leak infra concerns into domain (e.g. IDs typed as `ObjectId`, Spring annotations, Mongo-specific types).

Arguments (optional): $ARGUMENTS
