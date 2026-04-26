---
name: spring-boot-config
description: Spring Boot 4 configuration patterns for this project. Covers @Configuration bean wiring, env var loading via AppConfig, @ConfigurationProperties, profile selection, @Order on filter chains, and how to add new config classes. Trigger when writing or reviewing @Configuration classes, adding a new bean, wiring use cases, handling environment variables, or asking about config/properties.
---

# Spring Boot Config Skill

Spring Boot 4 + Kotlin. Jakarta namespace. `jakarta.*` not `javax.*`.

## Env var loading — `AppConfig`

`config/AppConfig.kt` loads env files in local dev; falls back to system env in deployment.

```
Priority: system env (Render/CI/k8s) > env/.env.{APP_ENVIRONMENT} > warning + fallback
```

- `APP_ENVIRONMENT` controls which file loads (`development`, `staging`, `production`).
- Deployment detected by: `RENDER`, `CI`, `KUBERNETES_SERVICE_HOST`, `DYNO`, `AWS_EXECUTION_ENV`, `GOOGLE_CLOUD_PROJECT`.
- Never hardcode secrets. Never commit env files.

## Bean wiring — use case config

Use cases are plain Kotlin classes (no Spring annotations in `application/`). Infra wires them:

```kotlin
// config/UserBeanConfig.kt
@Configuration
class UserBeanConfig {
    @Bean
    fun createUserUseCase(repo: IUserRepository, hasher: IPasswordHasherService): ICreateUserUseCase =
        CreateUserUseCase(repo, hasher)
}
```

Rules:
- One `@Configuration` class per feature (e.g. `UserBeanConfig`, `ImageBeanConfig`).
- Return type = port interface, not impl class.
- Constructor-inject all deps — no `lateinit var`, no field injection.

## @ConfigurationProperties (typed config)

For blocks of related settings:

```kotlin
@ConfigurationProperties(prefix = "app.ai")
data class AiProperties(
    val model: String,
    val maxTokens: Int,
    val endpoint: String,
)
```

Register at `@SpringBootApplication` class or a `@Configuration`:

```kotlin
@EnableConfigurationProperties(AiProperties::class)
```

Use `application.yml` for structure; env vars override via `APP_AI_MODEL=...` convention.

## Filter chain ordering — `@Order`

Two `SecurityFilterChain` beans exist. Spring picks by `@Order`:

| Order | Chain                         | Matcher                           |
|-------|-------------------------------|-----------------------------------|
| 1     | `actuatorSecurityFilterChain` | `EndpointRequest.toAnyEndpoint()` |
| 2     | `apiSecurityFilterChain`      | default (no matcher = catch-all)  |

Adding a third chain: pick an order between 1 and 2 or after 2. Never omit `@Order` when multiple chains exist.

## Cross-cutting config placement

| Concern                 | File                                      |
|-------------------------|-------------------------------------------|
| Env var loading         | `config/AppConfig.kt`                     |
| Security filter chain   | `config/SecurityConfig.kt`                |
| OpenAPI docs            | `config/OpenApiConfig.kt`                 |
| Feature use case wiring | `{feature}/config/{Feature}BeanConfig.kt` |
| Typed properties        | `config/{Name}Properties.kt`              |

## Rules

- No `@Component`/`@Service` in `application/` or `domain/`. Only `@Configuration` + `@Bean` in infra.
- Profile-scoped beans → `@Profile("test")` on the config class.
- Conditional beans → `@ConditionalOnProperty(name = "feature.x.enabled", havingValue = "true")`.
- Secrets via env vars only. Never `@Value("${secret}")` with a default that leaks.
