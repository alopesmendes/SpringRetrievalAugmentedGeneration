package com.ailtontech.user.adapter.secondary.security

import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.valueobjects.PasswordHash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasherService : IPasswordHasherService {
    private val encoder = BCryptPasswordEncoder()

    override fun hashPassword(password: String): PasswordHash {
        val hashedValue = encoder.encode(password)
        return PasswordHash.from(hashedValue)
    }
}
