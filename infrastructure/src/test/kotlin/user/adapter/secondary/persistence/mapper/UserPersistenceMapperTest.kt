package com.ailtontech.user.adapter.secondary.persistence.mapper

import com.ailtontech.user.adapter.secondary.persistence.document.UserDocument
import com.ailtontech.user.adapter.secondary.persistence.mapper.UserPersistenceMapper.toDocument
import com.ailtontech.user.adapter.secondary.persistence.mapper.UserPersistenceMapper.toDomain
import com.ailtontech.user.entity.User
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

@DisplayName("UserPersistenceMapperTest")
class UserPersistenceMapperTest {
    @Test
    fun `when mapping 'User', then return 'UserDocument'`() {
        val now = Instant.now()
        val user =
            User.from(
                id = UserId.from("user_id"),
                age = Age.from(20),
                password = PasswordHash.from("password_hash"),
                firstName = Name.from("Jane"),
                lastName = Name.from("Doe"),
                email = Email.from("jane@doe.com"),
                createdAt = now,
                updatedAt = now,
            )

        val result = user.toDocument()

        assertEquals(
            result,
            UserDocument(
                id = "user_id",
                age = 20,
                email = "jane@doe.com",
                firstName = "Jane",
                lastName = "Doe",
                passwordHash = "password_hash",
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    @Test
    fun `when mapping 'UserDocument', then return 'User'`() {
        val now = Instant.now()
        val userDocument =
            UserDocument(
                id = "user_id",
                email = "jane@doe.com",
                age = 20,
                passwordHash = "password_hash",
                firstName = "Jane",
                lastName = "Doe",
                createdAt = now,
                updatedAt = now,
            )

        val result = userDocument.toDomain()

        assertEquals(
            result,
            User.from(
                id = UserId.from("user_id"),
                age = Age.from(20),
                password = PasswordHash.from("password_hash"),
                firstName = Name.from("Jane"),
                lastName = Name.from("Doe"),
                email = Email.from("jane@doe.com"),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
