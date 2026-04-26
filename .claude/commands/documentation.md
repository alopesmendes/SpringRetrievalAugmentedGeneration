---
description: Write or update documentation — feature doc, GRAPH.md sync, CLAUDE.md update, or API contract.
---

You are helping write or update documentation for this project.

Before writing anything, ask:
1. **What kind** — feature doc, `{module}/docs/GRAPH.md` sync, `CLAUDE.md` update, `docs/API_CONTRACTS.md`, or `docs/TEST_STRATEGY.md`?
2. **Scope** — which feature, module, or class is being documented?
3. **Trigger** — new file added, file renamed/deleted, behavior changed, or first-time doc?

Wait for answers. Then:

**Feature doc**
- File: `{module}/docs/{FeatureName}.md`
- Sections: Purpose, Key classes (with paths), Invariants/rules, Sequence (Mermaid if helpful), Open questions.
- Max ~150 lines. Split if bigger.
- After writing: trigger `doc-graph-updater` agent to sync `{module}/docs/GRAPH.md`.

**GRAPH.md sync**
- Use `doc-graph-updater` agent. Do not hand-edit GRAPH.md directly unless agent is unavailable.
- Format: pure `mindmap` Mermaid block. One node per doc file. No edges.

**CLAUDE.md update**
- Root `CLAUDE.md`: cross-cutting rules only. No module-specific content.
- `{module}/CLAUDE.md`: module-specific rules only. No repetition of root rules.
- Keep files under 300 lines. Extract to skill if a section grows large.

**API_CONTRACTS.md / TEST_STRATEGY.md**
- These are reference docs. Update when contracts or test strategy actually change — not speculatively.

Rules:
- No doc for what the code already makes obvious.
- Comment only when WHY is non-obvious (hidden constraint, workaround, subtle invariant).
- Never document TODO items in code comments — use GitHub issues.

Arguments (optional): $ARGUMENTS
