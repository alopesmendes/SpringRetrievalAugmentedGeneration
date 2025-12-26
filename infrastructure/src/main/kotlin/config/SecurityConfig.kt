package com.ailtontech.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
                // Allow health endpoint without authentication (for Render/K8s health checks)
                authorize
                    .requestMatchers(EndpointRequest.to(HealthEndpoint::class.java))
                    .permitAll()
                    .requestMatchers(EndpointRequest.to("health", "info"))
                    .permitAll()
                    // Require authentication for other actuator endpoints
                    .anyRequest()
                    .authenticated()
            }.httpBasic { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.csrf { csrf -> csrf.disable() }

        return http.build()
    }

    /**
     * Security filter chain for API endpoints.
     */
    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests { authorize ->
                // Public endpoints
                authorize
                    .requestMatchers(EndpointRequest.to(HealthEndpoint::class.java))
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated()
            }.httpBasic { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.csrf { csrf -> csrf.disable() }

        return http.build()
    }
}
