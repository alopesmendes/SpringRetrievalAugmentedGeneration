package com.ailtontech.user.mapper

import com.ailtontech.user.dto.UserResult
import com.ailtontech.user.entity.User

object UserMapper {
    fun User.toResult() = UserResult(
        id = id.value,
        email = email.value,
        age = age.value,
        firstName = firstName.capitalized,
        lastName = lastName.capitalized,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
