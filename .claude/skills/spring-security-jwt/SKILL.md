---
name: spring-security-jwt
description: Spring Security configuration patterns for this project. Covers the two-chain SecurityFilterChain setup, BCrypt password hashing, stateless session policy, CSRF configuration, and the JWT upgrade path when auth is added. Trigger when writing or reviewing SecurityConfig, adding authentication, JWT tokens, BCrypt, @PreAuthorize, or securing new endpoints.
---

# Spring Security JWT Skill

Spring Security 6 + Spring Boot 4 + `jakarta.*`. Stateless. Two filter chains ordered by `@Order`.

## Current state

Auth today: **httpBasic + BCrypt**. JWT not yet implemented. SecurityConfig lives in `config/SecurityConfig.kt`.

Two filter chains:

```kotlin
@Order(1) fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain
@Order(2) fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain
```

`BCryptPasswordEncoder` bean registered in `SecurityConfig`. Adapter `BCryptPasswordHasherService` implements `IPasswordHasherService` output port.

## Filter chain pattern

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    @Order(1)
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests {
                it.requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
                it.requestMatchers(EndpointRequest.to("info")).permitAll()
                it.anyRequest().authenticated()
            }
            .httpBasic { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.ignoringRequestMatchers(EndpointRequest.toAnyEndpoint()) }
        return http.build()
    }

    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                it.requestMatchers("/api/v1/users/**").permitAll()
                it.anyRequest().authenticated()
            }
            .httpBasic { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.ignoringRequestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**", "/error") }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

## BCrypt adapter

```kotlin
// adapter/secondary/security/BCryptPasswordHasherService.kt
@Service
class BCryptPasswordHasherService(private val encoder: PasswordEncoder) : IPasswordHasherService {
    override fun hash(raw: String): PasswordHash = PasswordHash(encoder.encode(raw))
    override fun matches(raw: String, hash: PasswordHash): Boolean = encoder.matches(raw, hash.value)
}
```

Port `IPasswordHasherService` defined in `application/port/secondary/`. Adapter in infra.

## JWT upgrade path (when needed)

When adding JWT:
1. Define `ITokenService` output port in `application/port/secondary/`.
2. Implement `JwtTokenService` in `infrastructure/adapter/secondary/security/`.
3. Add `JwtAuthenticationFilter : OncePerRequestFilter` in infra.
4. Register filter in `apiSecurityFilterChain` via `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)`.
5. Replace `.httpBasic { }` with `.httpBasic { it.disable() }` on the API chain.
6. Keep httpBasic on actuator chain — internal use only.

JWT bean wiring: add to existing `UserBeanConfig` or a new `SecurityBeanConfig`. Never hardcode secret — read from env var via `@Value("\${jwt.secret}")`.

## Securing new endpoints

Public endpoint (no auth):
```kotlin
it.requestMatchers("/api/v1/auth/**").permitAll()
```

Authenticated endpoint (any valid token):
```kotlin
it.requestMatchers("/api/v1/images/**").authenticated()
```

Role-based (method level):
```kotlin
@PreAuthorize("hasRole('ADMIN')")
fun deleteUser(@PathVariable id: String): Unit
```

Enable method security: `@EnableMethodSecurity` on `SecurityConfig`.

## CSRF rules

CSRF disabled for:
- All API routes (`/api/**`) — stateless, no cookies.
- Swagger routes — tooling access.
- Actuator routes — internal.

Never disable CSRF globally. Target only routes that need it.

## What to ask user before adding auth

- Which endpoints become protected?
- JWT or session-based?
- Token expiry? Refresh token needed?
- Where do secrets live (env var name)?
- Role model needed (`USER`, `ADMIN`)?
