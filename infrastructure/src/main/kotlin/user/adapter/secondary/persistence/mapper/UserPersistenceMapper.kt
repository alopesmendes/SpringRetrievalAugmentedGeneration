package com.ailtontech.user.adapter.secondary.persistence.mapper

import com.ailtontech.user.adapter.secondary.persistence.document.UserDocument
import com.ailtontech.user.entity.User
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId

/**
 * The mapper object that have the extension functions for the persistence models (documents)
 */
object UserPersistenceMapper {
    /**
     * Extension method that will map [UserDocument] into a [User]
     *
     * @return The [User] that represents the [UserDocument]
     */
    fun UserDocument.toDomain(): User = User.from(
        id = UserId.from(id),
        email = Email.from(email),
        firstName = Name.from(firstName),
        lastName = Name.from(lastName),
        age = Age.from(age),
        password = PasswordHash.from(passwordHash),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    /**
     * Extension method that will map [User] into a [UserDocument]
     *
     * @return The [UserDocument] that represents the [User]
     */
    fun User.toDocument(): UserDocument = UserDocument(
        id = id.value,
        email = email.value,
        firstName = firstName.value,
        lastName = lastName.value,
        age = age.value,
        passwordHash = password.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
