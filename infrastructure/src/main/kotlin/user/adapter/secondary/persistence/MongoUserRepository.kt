package com.ailtontech.user.adapter.secondary.persistence

import com.ailtontech.user.entity.User
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.UserId
import org.springframework.stereotype.Repository

@Repository
class MongoUserRepository : IUserRepository {
    private val users: MutableMap<UserId, User> = mutableMapOf()

    override fun existsByEmail(email: Email): Boolean = users.values.any { it.email == email }

    override fun save(user: User): User {
        users[user.id] = user
        return user
    }

    override fun findById(id: UserId): User? = users[id]
}
