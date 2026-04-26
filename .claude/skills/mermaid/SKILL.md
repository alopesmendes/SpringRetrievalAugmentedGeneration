---
name: mermaid
description: Use when writing or editing Mermaid diagrams — flowchart, architecture-beta, sequence, class, ER, C4, state. Triggers on requests to draw/visualize systems, document architecture, build doc graphs (GRAPH.md), explain flows, or fix invalid Mermaid syntax. Covers diagram type selection, syntax patterns, theming, and Claude-Code-specific GRAPH.md conventions for this project.
---

# Mermaid Skill

Diagrams as code. Render in any Markdown that runs Mermaid (GitHub, GitLab, VS Code, mermaid.live). This project uses Mermaid for: hexagonal architecture diagrams, per-module doc graphs (`{module}/docs/GRAPH.md`), sequence flows in feature docs, ADRs.

## Pick the right diagram

| Use case                                              | Diagram                       |
|-------------------------------------------------------|-------------------------------|
| Doc graph, generic node-edge model                    | `flowchart TD`                |
| System / cloud architecture (db, server, queue)       | `architecture-beta`           |
| Use-case call sequence (controller → use case → port) | `sequenceDiagram`             |
| Domain model (entities + relations)                   | `classDiagram` or `erDiagram` |
| State machine of an entity                            | `stateDiagram-v2`             |
| C4 model (context/container/component/code)           | `C4Context` etc.              |

Default to `flowchart` unless one of the others is clearly better.

## Flowchart — primary syntax

```mermaid
flowchart TD
    A[Rectangle] --> B(Rounded)
    B --> C{Decision}
    C -->|yes| D([Stadium])
    C -.->|no| E[[Subroutine]]
    D --- F((Circle))
    classDef warn fill:#fecaca,stroke:#991b1b
    class C warn
    click A "./other.md" "open"
```

Direction: `TD` `LR` `BT` `RL`. Shapes: `[]` rect · `()` rounded · `{}` diamond · `([])` stadium · `[[]]` subroutine · `(())` circle · `[/...\]` parallelogram. Edges: `-->` arrow · `---` line · `-.->` dotted · `==>` thick · `-->|label|` labeled. Subgraphs:

```mermaid
flowchart LR
    subgraph S1[Domain]
        E[User]
    end
    subgraph S2[Application]
        UC[CreateUserUseCase]
    end
    UC --> E
```

Click directives: `click NodeId "url" "tooltip"` opens link · `click NodeId callback "tooltip"` calls JS. Use to make doc-graph nodes navigable.

## Architecture-beta — for system topology

```mermaid
architecture-beta
    group api(cloud)[API]

    service db(database)[Mongo] in api
    service rest(server)[REST] in api
    service ai(internet)[Spring AI]

    rest:R --> L:db
    rest:T --> B:ai
```

Edges anchor on side: `T B L R`. Default icons: `cloud database disk internet server`. Iconify icons via `"prefix:name"` (e.g. `"logos:kotlin"`). Junctions: `junction j1` for 4-way splits.

## Sequence — for call flows

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctl as UserController
    participant UC as CreateUserUseCase
    participant Repo as IUserRepository
    C->>Ctl: POST /users
    Ctl->>UC: invoke(command)
    UC->>Repo: findByEmail
    Repo-->>UC: null
    UC->>Repo: save(user)
    Repo-->>UC: User
    UC-->>Ctl: Result.success
    Ctl-->>C: 201 Created
```

Arrows: `->>` solid · `-->>` dashed reply · `-x` cross (lost). `Note over A,B:` for callouts. `loop` `alt` `opt` for control flow.

## Class diagram — domain model

```mermaid
classDiagram
    class User {
        +UserId id
        +Email email
        +create() User$
    }
    class Email { +String value }
    User *-- Email
    User ..> UserException : throws
```

Relations: `<|--` inheritance · `*--` composition · `o--` aggregation · `-->` association · `..>` dependency · `..|>` realization.

## Theming, colors, classDef

```
classDef entity      fill:#fde68a,stroke:#92400e
classDef valueobject fill:#bfdbfe,stroke:#1e3a8a
class User entity
class Email,Name valueobject
```

Init block at top to switch theme:

````
%%{init: {'theme':'dark'}}%%
flowchart TD ...
````

## Project-specific: `GRAPH.md` files

Each module has `{module}/docs/GRAPH.md` whose **only** content is one fenced ```mermaid block.

Conventions (decided 2026-04-26 — Option A: hierarchical index, no edges):
- Diagram type: `mindmap` (not flowchart). No cross-references between docs — `doc-graph-updater` is filename-only and does not parse content.
- Root: `root((<module name>))`.
- Branches under root = doc-type buckets (e.g. `Entities`, `ValueObjects`, `UseCases`, `RestControllers`, ...).
- Leaves under each bucket = `.md` filenames (one per file, basename without `.md`).
- 2-space indentation per level. Mismatched indentation breaks rendering.
- Empty buckets: keep the bucket node with no children — do not add a placeholder leaf.
- Do NOT include `GRAPH.md` itself as a leaf.

Bucket list per module is defined in the `doc-graph-updater` agent prompt.

### Mindmap shapes available

`A` plain · `A[text]` square · `A(text)` rounded · `A((text))` circle · `A{{text}}` cloud/hexagon · `A))text((` bang. Conventional: root uses `((...))` circle.

### Empty state (current 3 modules)

```mermaid
mindmap
  root((domain))
    Entities
    ValueObjects
    Services
    Exceptions
    Annotations
```

### Populated state (target shape after docs land)

```mermaid
mindmap
  root((domain))
    Entities
      User
    ValueObjects
      Age
      Email
      Name
      PasswordHash
      UserId
    Services
      PasswordService
    Exceptions
      UserException
    Annotations
      DomainEntity
      DomainService
      ValueObject
```

### Why mindmap (not flowchart)

Mindmap is a strict tree — no cross-edges between branches. Picked because the agent only tracks filenames, not content. If the agent ever needs to express "User depends on Email" or "controller calls use case", switch to `flowchart TD` with `subgraph`s — see `mermaid` flowchart section above.

## Validation checklist

Before saving a diagram:
- [ ] Block is fenced ```` ```mermaid ```` (not `` ` ``mermaid).
- [ ] Diagram type declared on first non-comment line.
- [ ] No empty `subgraph ... end` (renderer errors).
- [ ] Node ids are unique and ASCII-safe (`[A-Za-z0-9_]`). Quotes for labels with special chars: `A["my label"]`.
- [ ] Edge labels escape pipes inside text via quotes: `A -->|"a|b"| B`.
- [ ] `classDef` declared before `class` use.
- [ ] Render-test in https://mermaid.live before committing big diagrams.

## Common pitfalls

- Mixing `graph` and `flowchart` keywords — both work, but `flowchart` supports more shapes. Prefer `flowchart`.
- Forgetting `end` for each `subgraph` (errors silently in some renderers).
- `architecture-beta` is still beta — pin Mermaid version if rendering server-side.
- Long labels overflow — wrap with `<br/>` or shorten ids and rely on tooltip via `click`.
- Markdown inside node labels: use `markdown` shape (`A@{shape: rect, label: "**bold**"}`) — Mermaid 11.3+.

## References

- Syntax index — https://mermaid.js.org/intro/syntax-reference.html
- Flowchart — https://mermaid.js.org/syntax/flowchart.html
- Architecture (beta) — https://mermaid.js.org/syntax/architecture.html
- Sequence — https://mermaid.js.org/syntax/sequenceDiagram.html
- Class — https://mermaid.js.org/syntax/classDiagram.html
- ER — https://mermaid.js.org/syntax/entityRelationshipDiagram.html
- State — https://mermaid.js.org/syntax/stateDiagram.html
- C4 — https://mermaid.js.org/syntax/c4.html
- Theming — https://mermaid.js.org/config/theming.html
- Live editor — https://mermaid.live
