package com.ailtontech.user.adapter.secondary.persistence.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

/**
 * The document that represents a User and will be saved in mongodb
 *
 * @property id The unique identifier of a user
 * @property email The email of a user
 * @property age The age of a user
 * @property passwordHash The hash password of a user
 * @property firstName The first name of a user
 * @property lastName The last name of a user
 * @property createdAt The date the user was created
 * @property updatedAt The date the user was updated
 */
@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String,
    @Field(name = "email")
    val email: String,
    @Field(name = "age")
    val age: Int,
    @Field(name = "password_hash")
    val passwordHash: String,
    @Field(name = "first_name")
    val firstName: String,
    @Field(name = "last_name")
    val lastName: String,
    @Field("created_at")
    val createdAt: Instant,
    @Field("updated_at")
    val updatedAt: Instant,
)
