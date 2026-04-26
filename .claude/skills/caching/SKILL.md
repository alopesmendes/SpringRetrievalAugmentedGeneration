---
name: caching
description: Caching patterns for this project. Covers Spring Cache abstraction, @Cacheable/@CacheEvict placement rules, output port wrapping for caches, Redis vs in-memory, and when NOT to cache. Trigger when adding caching, @Cacheable, @CacheEvict, Redis config, or asking about cache invalidation or cache-aside pattern.
---

# Caching Skill

Spring Cache abstraction. Cache is an infra concern — domain and application never import cache annotations.

## Placement rules

Cache annotations go on the **persistence adapter** implementing the output port — never on use cases, domain services, or ports.

```kotlin
// infrastructure/adapter/secondary/persistence/UserRepository.kt
@Repository
class UserRepository(private val mongo: MongoUserRepository) : IUserRepository {

    @Cacheable("users", key = "#email.value")
    override fun findByEmail(email: Email): User? =
        mongo.findByEmail(email.value)?.toDomain()

    @CacheEvict("users", key = "#user.email.value")
    override fun save(user: User): User = mongo.save(user.toDocument()).toDomain()
}
```

If caching needs its own port (e.g. distributed cache as a primary store), define `ICacheService` in `application/port/secondary/` and implement in infra.

## Enable caching

```kotlin
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager("users", "images")
}
```

For Redis:

```kotlin
@Bean
fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager =
    RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues(),
        )
        .build()
```

Redis URI from env var: `SPRING_DATA_REDIS_URL`. Never hardcoded.

## application.yml (Redis)

```yaml
spring:
  data:
    redis:
      url: ${REDIS_URL}
      timeout: 2000ms
  cache:
    type: redis
```

## Cache invalidation

`@CacheEvict` on write operations. Match key expression to `@Cacheable`.

Evict all entries for a cache name (use sparingly — expensive):

```kotlin
@CacheEvict("users", allEntries = true)
override fun deleteById(id: UserId) { ... }
```

## When NOT to cache

- Write-heavy data with no read amplification benefit.
- Data with sub-millisecond DB access (cached > DB latency is unusual).
- PII data requiring strict access audit — cache hits bypass audit trail.
- Anything with complex invalidation logic that domain can't reason about.

## What to ask user before adding cache

- Which operation is slow (read? write?)?
- Acceptable staleness? (TTL choice)
- Cache scope: local (single instance) or distributed (Redis)?
- Invalidation trigger: write-through, TTL-only, or event-based?
- Key: field, composite, or full object hash?
