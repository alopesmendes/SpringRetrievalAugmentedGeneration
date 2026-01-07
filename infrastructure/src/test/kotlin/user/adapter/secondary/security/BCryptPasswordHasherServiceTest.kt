package com.ailtontech.user.adapter.secondary.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("BCryptPasswordHasherServiceTest")
class BCryptPasswordHasherServiceTest {
    private lateinit var service: BCryptPasswordHasherService
    private val encoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setUp() {
        service = BCryptPasswordHasherService()
    }

    @Test
    fun `given a raw password when hashing then result should be a valid BCrypt hash different from input`() {
        val rawPassword = "SecurePassword123!"

        val result = service.hashPassword(rawPassword)

        assertNotEquals(rawPassword, result.value)
        assertTrue(encoder.matches(rawPassword, result.value))
    }

    @Test
    fun `given the same password when hashing twice then result should produce different hashes due to random salt`() {
        val rawPassword = "SecurePassword123!"

        val hash1 = service.hashPassword(rawPassword)
        val hash2 = service.hashPassword(rawPassword)

        // BCrypt uses a random salt by default
        assertNotEquals(hash1.value, hash2.value)
        assertTrue(encoder.matches(rawPassword, hash1.value))
        assertTrue(encoder.matches(rawPassword, hash2.value))
    }
}
