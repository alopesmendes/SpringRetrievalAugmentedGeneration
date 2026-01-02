package com.ailtontech.user.mapper

import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.entity.User
import com.ailtontech.user.mapper.UserMapper.toResult
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("UserMapperTest")
class UserMapperTest {
    @Test
    fun `when mapping from user, then return 'UserResult'`() {
        val date = Instant.now()
        val user =
            User.from(
                id = UserId.from("user_id"),
                firstName = Name.from("Jane"),
                lastName = Name.from("Doe"),
                email = Email.from("jane@doe.com"),
                password = PasswordHash.from("passwordhash"),
                age = Age.from(25),
                createdAt = date,
                updatedAt = date,
            )

        val userResult = user.toResult()

        assertEquals(
            userResult,
            UserResult(
                id = "user_id",
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@doe.com",
                age = 25,
                createdAt = date,
                updatedAt = date,
            ),
        )
    }

    @Test
    fun `when mapping from user, then return 'UserResult' with capitalized names`() {
        val date = Instant.now()
        val user =
            User.from(
                id = UserId.from("user_id"),
                firstName = Name.from("jane"),
                lastName = Name.from("doe"),
                email = Email.from("jane@doe.com"),
                password = PasswordHash.from("passwordhash"),
                age = Age.from(25),
                createdAt = date,
                updatedAt = date,
            )

        val userResult = user.toResult()

        assertEquals(
            userResult,
            UserResult(
                id = "user_id",
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@doe.com",
                age = 25,
                createdAt = date,
                updatedAt = date,
            ),
        )
    }
}
