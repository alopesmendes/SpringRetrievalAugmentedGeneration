package com.ailtontech.user.adapter.secondary.persistence

import com.ailtontech.user.adapter.secondary.persistence.mapper.UserPersistenceMapper.toDocument
import com.ailtontech.user.adapter.secondary.persistence.mapper.UserPersistenceMapper.toDomain
import com.ailtontech.user.entity.User
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.UserId
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepository(
    private val mongoUserRepository: MongoUserRepository,
) : IUserRepository {
    override fun existsByEmail(email: Email): Boolean = mongoUserRepository.existsByEmail(email.value)

    override fun save(user: User): User {
        val document = user.toDocument()
        val user = mongoUserRepository.save(document)
        return user.toDomain()
    }

    override fun findById(id: UserId): User? = mongoUserRepository
        .findById(id.value)
        .getOrNull()
        ?.toDomain()
}
