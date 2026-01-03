package com.ailtontech.user.port.secondary

import com.ailtontech.user.valueobjects.PasswordHash

interface IPasswordHasherService {
    fun hashPassword(password: String): PasswordHash
}
