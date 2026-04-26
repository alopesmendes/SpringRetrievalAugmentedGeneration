# Claude Code Setup — Progress Tracker

Track build of Claude Code config (skills, commands, agents, module CLAUDE.md). Issue #119.

## Principles

- Files <300 lines. Bigger → split.
- Skill `SKILL.md` body cold-loaded only when description matches. Keep examples there too unless heavy.
- Domain + application skills: tech-agnostic (no Spring/Mongo). Infra skills: Spring Boot OK.
- Always ask permission before write.
- Challenger tone: ask first, code last. Doubt → ask user.
- Goal: collaborator that knows product, not code-vending machine.

## Layout

```
.claude/
  agents/    — task-scoped subagents
  skills/    — model-selected via description
  commands/  — user-invoked /commands at project root
CLAUDE.md            — slim root, links to module
{module}/CLAUDE.md   — per-module rules
{module}/docs/GRAPH.md — doc index per module (used by doc-graph-updater)
```

## Status

Legend: `[ ]` todo · `[~]` in progress · `[x]` done · `[—]` deferred

### Batch 1 — Foundation
- [x] `docs/CLAUDE_PROGRESS.md` (this file)
- [x] `CLAUDE.md` slim rewrite
- [x] `domain/CLAUDE.md`
- [x] `application/CLAUDE.md`
- [x] `infrastructure/CLAUDE.md`
- [x] `domain/docs/GRAPH.md`
- [x] `application/docs/GRAPH.md`
- [x] `infrastructure/docs/GRAPH.md`

### Batch 1.5 — Mermaid + GRAPH.md format
- [x] `.claude/skills/mermaid/SKILL.md`
- [x] Rewrite `domain/docs/GRAPH.md` as pure mermaid (mindmap)
- [x] Rewrite `application/docs/GRAPH.md` as pure mermaid (mindmap)
- [x] Rewrite `infrastructure/docs/GRAPH.md` as pure mermaid (mindmap)
- [x] Update `doc-graph-updater` agent prompt for mindmap format
- [x] Update `mermaid` skill GRAPH.md section to mindmap convention

### Batch 2 — Core skills (tech-agnostic)
- [x] `.claude/skills/hexagonal-architecture/SKILL.md`
- [x] `.claude/skills/kotlin-style/SKILL.md`
- [x] `.claude/skills/clean-code/SKILL.md`
- [x] `.claude/skills/error-handling/SKILL.md`
- [x] `.claude/skills/testing-strategy/SKILL.md`
- [x] `.claude/skills/documentation/SKILL.md`

### Batch 3 — Domain/application skills
- [x] `.claude/skills/domain-modeling/SKILL.md`
- [x] `.claude/skills/api-design/SKILL.md` (port-level interface contract, layer-agnostic)

### Batch 4 — Infra skills (Spring Boot allowed)
- [x] `.claude/skills/spring-boot-config/SKILL.md`
- [x] `.claude/skills/spring-rest-mvc/SKILL.md`
- [x] `.claude/skills/spring-data-mongo/SKILL.md`
- [x] `.claude/skills/spring-security-jwt/SKILL.md`
- [x] `.claude/skills/spring-testcontainers/SKILL.md`
- [x] `.claude/skills/observability/SKILL.md`
- [x] `.claude/skills/caching/SKILL.md`

### Batch 5 — Commands
- [x] `.claude/commands/test.md`
- [x] `.claude/commands/domain.md`
- [x] `.claude/commands/application.md`
- [x] `.claude/commands/infrastructure.md`
- [x] `.claude/commands/feature.md`
- [x] `.claude/commands/documentation.md`

### Batch 6 — New agents
- [x] `.claude/agents/code-reviewer.md`
- [x] `.claude/agents/security-engineer.md`
- [x] `.claude/agents/test-automator.md`

### Batch 7 — Cleanup
- [x] Port any remaining `.clinerules/*.md` content into skills if missed
- [x] Delete `.clinerules/` dir + `AGENTS.md` (superseded)
- [x] Update `.gitignore` for `.claude/agent-memory/*/MEMORY.md` if private

## Roadmap (write later, on demand)
- `event-sourcing` skill — write when first aggregate uses events
- `notification-messaging` skill — write when first SNS/SQS/SES adapter
- `kotlin-ktor-migration` skill — write only if/when Spring → Ktor migration starts

## Conventions log

- Skill dir = lowercase-kebab-case. `SKILL.md` filename uppercase.
- Command file = lowercase, no hyphens (`feature.md`, not `create-feature.md`) — invoked as `/feature`.
- Agent frontmatter must include `name`, `description`, `tools`, `model`, `color`. Optional `memory: project`.
- Module CLAUDE.md describes only what's true for that module. Cross-cutting → root CLAUDE.md or skill.
- Examples colocated with skill that needs them. Cold until skill triggers.

## Decisions log

- 2026-04-26: `.clinerules/` content ported into skills + module CLAUDE.md, then dir deleted (single source of truth).
- 2026-04-26: Skill list final per Batch 2-4 above. `notification-messaging`, `event-sourcing` deferred to roadmap (no code uses them yet).
- 2026-04-26: Spring-specific skills under infra accepted as swappable — Ktor migration handled by future `kotlin-ktor-migration` skill, not by abstracting infra skills now.
- 2026-04-26: Commands at root `.claude/commands/`, not per-module. Each command asks which module to target.
- 2026-04-26: GRAPH.md = pure mermaid block. `mermaid` skill added to support authoring (not in original list).
- 2026-04-26: GRAPH.md uses `mindmap` (Option A — index only, no edges). Picked because `doc-graph-updater` agent reads filenames only, never file content. Cross-references would require content parsing. Switch to `flowchart TD` later if needed.
