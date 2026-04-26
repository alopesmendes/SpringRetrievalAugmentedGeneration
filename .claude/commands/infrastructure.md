---
description: Design infrastructure adapters — REST controller, persistence adapter, security config, or outbound service.
---

You are helping design infrastructure adapters. Spring + framework code lives here. Domain and application layers must stay clean.

Before writing anything, ask:
1. **Adapter type** — inbound REST, outbound persistence (Mongo), security config, or outbound external service?
2. **Feature** — which feature context? (e.g. `user`, `image`, `retrieval`)
3. **Port** — which input or output port does this adapter implement or call?

Wait for answers. Propose structure before writing code. User confirms before any file is written.

Rules by adapter type:

**REST (inbound)**
- `@RestController` in `adapter/primary/rest/`. No business logic.
- DTOs: `{Entity}{Purpose}Dto` — `@Valid` on request bodies.
- Mapper: `{Layer}{Entity}Mapper` as extension functions. Lives at layer boundary.
- Fold `Result<T>` to HTTP status in `GlobalExceptionHandler`, not in controller.
- springdoc-openapi annotations on all endpoints.

**Persistence (outbound)**
- `{Entity}Document` with `@Document`. Separate from domain entity.
- `{Entity}MongoRepository` extends `MongoRepository`.
- `{Entity}PersistenceAdapter` implements output port. Injects `MongoRepository`. Uses mapper.
- Mapper: extension functions `User.toDocument()` / `UserDocument.toDomain()`.

**Security**
- Two `SecurityFilterChain` beans: one for public paths, one for JWT-protected paths.
- `BCryptPasswordEncoder` bean. Stateless session. CSRF disabled for API.
- JWT filter chain only. No session cookies.

**Config**
- `@Configuration` classes only. Wire use cases as `@Bean`. No `@Service` in application module.

Challenge any adapter that contains business logic, decision branching on domain state, or imports from `domain/` beyond reading value objects.

Arguments (optional): $ARGUMENTS
