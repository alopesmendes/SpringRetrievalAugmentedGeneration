package com.ailtontech.user.adapter.primary.rest.mapper

import com.ailtontech.user.adapter.primary.rest.dto.CreateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.dto.UpdateUserRequestDto
import com.ailtontech.user.adapter.primary.rest.mapper.UserRestMapper.toCommand
import com.ailtontech.user.dto.CreateUserCommand
import com.ailtontech.user.dto.UpdateUserCommand
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("UserRestMapperTest")
class UserRestMapperTest {
    @Test
    fun `when mapping 'CreateUserRequestDto', then return 'CreateUserCommand'`() {
        val createUserRequestDto =
            CreateUserRequestDto(
                email = "jane@doe.com",
                age = 30,
                password = "SecuP@ss123",
                firstName = "jane",
                lastName = "doe",
            )

        val result = createUserRequestDto.toCommand()

        assertEquals(
            result,
            CreateUserCommand(
                email = "jane@doe.com",
                age = 30,
                rawPassword = "SecuP@ss123",
                firstName = "jane",
                lastName = "doe",
            ),
        )
    }

    @Test
    fun `when mapping 'UpdateUserRequestDto', then return 'UpdateUserCommand'`() {
        val updateUserRequestDto =
            UpdateUserRequestDto(
                email = "jane@doe.com",
                age = 30,
                password = "SecuP@ss123",
                firstName = "jane",
                lastName = "doe",
            )

        val result = updateUserRequestDto.toCommand("user_id")

        assertEquals(
            result,
            UpdateUserCommand(
                id = "user_id",
                email = "jane@doe.com",
                age = 30,
                rawPassword = "SecuP@ss123",
                firstName = "jane",
                lastName = "doe",
            ),
        )
    }
}
