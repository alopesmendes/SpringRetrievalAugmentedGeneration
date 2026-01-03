package com.ailtontech.user.port.secondary

import com.ailtontech.user.entity.User
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.UserId

interface IUserRepository {
    fun existsByEmail(email: Email): Boolean

    fun save(user: User): User

    fun findById(id: UserId): User?
}
