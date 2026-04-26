---
name: observability
description: Observability patterns for this project. Covers Spring Boot Actuator endpoint security, SLF4J structured logging, Micrometer metrics, health indicators, and what NOT to log (secrets, PII). Trigger when adding logging, metrics, health checks, actuator config, or asking about monitoring and observability.
---

# Observability Skill

Spring Boot Actuator + SLF4J. Infra layer only. Domain/application have zero logging deps.

## Logging — SLF4J

Use `LoggerFactory` — no Spring logging wrapper.

```kotlin
private val logger = LoggerFactory.getLogger(MyClass::class.java)
```

- **INFO** — lifecycle events (app start, config loaded, external service connected).
- **WARN** — degraded but continuing (env file not found, fallback used).
- **ERROR** — unrecoverable failure before throw.
- **DEBUG** — adapter-level detail. Off in production.

Never log:
- Passwords, tokens, hashes (`passwordHash`, JWT).
- PII: email, name, age unless explicitly required and masked.
- Domain exceptions at ERROR — they're expected failures, log at WARN max.

Structured pattern (add to `application.yml`):

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Actuator

Configured in `config/SecurityConfig.kt` — health and info are public, all other endpoints require auth.

Expose via `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
  info:
    env:
      enabled: true
```

Public: `/actuator/health`, `/actuator/info`.
Authenticated (httpBasic): everything else.

## Health indicators

Custom health indicator for an external dependency:

```kotlin
@Component
class MongoHealthIndicator(private val mongo: MongoClient) : HealthIndicator {
    override fun health(): Health = try {
        mongo.getDatabase("admin").runCommand(Document("ping", 1))
        Health.up().build()
    } catch (e: Exception) {
        Health.down(e).build()
    }
}
```

Spring Boot auto-configures Mongo health — only write custom if the auto one doesn't cover the need.

## Metrics — Micrometer

Auto-configured. JVM, HTTP, Mongo metrics available out of the box.

Custom counter:

```kotlin
@Component
class UserMetrics(meterRegistry: MeterRegistry) {
    private val userCreated = meterRegistry.counter("users.created")
    fun recordCreation() = userCreated.increment()
}
```

Expose Prometheus scrape endpoint via `include: prometheus` in actuator config above.

## Tracing

Spring Boot 3+ includes Micrometer Tracing. Add dependency when needed:

```kotlin
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
```

Trace ID auto-injected in MDC — appears in logs if pattern includes `%X{traceId}`.

## What NOT to do

- No logging in domain or application modules — pure Kotlin, no SLF4J dep.
- No `System.out.println` — SLF4J always.
- No sensitive data in log messages, even at DEBUG.
- No custom health endpoint outside `@Component HealthIndicator` — use Actuator.
