package com.ailtontech.user.adapter.secondary.persistence

import com.ailtontech.user.adapter.secondary.persistence.document.UserDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoUserRepository : MongoRepository<UserDocument, String> {
    /**
     * Checks if a user exists with the given email.
     *
     * @param email The email to check
     * @return true if a user exists with this email
     */
    fun existsByEmail(email: String): Boolean
}
