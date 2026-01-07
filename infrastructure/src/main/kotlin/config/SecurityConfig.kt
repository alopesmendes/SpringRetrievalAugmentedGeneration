package com.ailtontech.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * Security configuration for the application.
 *
 * This configuration ensures that:
 * - Health endpoints are accessible without authentication (for Render health checks)
 * - Other actuator endpoints require authentication
 * - API endpoints are secured appropriately
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {
    /**
     * Security filter chain for actuator endpoints.
     * Health endpoint is permitted without authentication for external health checks.
     */
    @Bean
    @Order(1)
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(EndpointRequest.to(HealthEndpoint::class.java))
                    .permitAll()
                    .requestMatchers(EndpointRequest.to("info"))
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.httpBasic { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers(EndpointRequest.toAnyEndpoint())
            }

        return http.build()
    }

    /**
     * Security filter chain for API endpoints.
     */
    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // REMOVED: .securityMatcher(EndpointRequest.toAnyEndpoint())
            // By not specifying a securityMatcher, this chain acts as the default
            .authorizeHttpRequests { authorize ->
                authorize
                    // 1. Documentation & Errors
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/error",
                    ).permitAll()
                    // 2. Public User Endpoints
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    // 3. Everything else requires Auth
                    .anyRequest()
                    .authenticated()
            }.httpBasic { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/error",
                    "/api/**",
                )
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
