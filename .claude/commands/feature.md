---
description: Orchestrate a full feature across domain → application → infrastructure. Runs /domain, /application, /infrastructure in sequence.
---

You are helping design and implement a complete feature end-to-end using hexagonal architecture.

This command orchestrates three phases in strict order: domain → application → infrastructure.

**Step 0 — Understand the feature**

Before anything else, ask:
1. **Feature name** — what is this feature? (e.g. "upload image", "search by tags", "delete user")
2. **Entry point** — how is it triggered? (REST endpoint, event, scheduled job?)
3. **Persistence** — what gets stored or read?
4. **External deps** — any external service calls? (AI model, S3, email, etc.)

Wait for full answers. Then proceed phase by phase — never skip ahead.

---

**Phase 1 — Domain** (`/domain`)

Design entities, value objects, domain services, domain exceptions needed.
Propose. User confirms. Write domain code only.
Verify: zero Spring imports, invariants encoded, factory `create()` used.

---

**Phase 2 — Application** (`/application`)

Design input port, output port(s), use case, command, result.
Propose. User confirms. Write application code only.
Verify: zero Spring imports, `Result<T>` used, ports accept domain types.

---

**Phase 3 — Infrastructure** (`/infrastructure`)

Design REST controller, persistence adapter, any config beans, security if needed.
Propose. User confirms. Write infra code only.
Verify: adapter translates and delegates — zero business logic in adapters.

---

**After all phases:**
- Remind user to run `./gradlew unitTest` to catch Konsist violations.
- Ask if tests are needed → offer to run `/test`.
- Ask if docs need updating → offer to run `/documentation`.

Never write code from a later phase before the earlier phase is confirmed. Challenge scope creep.

Arguments (optional): $ARGUMENTS
