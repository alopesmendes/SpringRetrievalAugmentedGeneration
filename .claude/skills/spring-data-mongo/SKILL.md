---
name: spring-data-mongo
description: Spring Data MongoDB adapter patterns for this project. Covers @Document class design, MongoRepository interface, persistence adapter implementing the output port, mapper extension functions, field naming, indexes, and two-class split. Trigger when writing or reviewing Mongo documents, repositories, persistence adapters, persistence mappers, or Mongo collection design.
---

# Spring Data Mongo Skill

Outbound adapter. Implements output ports from `application/`. Never leaks Mongo into domain.

## Two-class split

Every entity needs two classes:

| Class                 | Purpose                                     | Spring annotation       |
|-----------------------|---------------------------------------------|-------------------------|
| `MongoUserRepository` | Spring Data interface — query methods       | `@Repository interface` |
| `UserRepository`      | Adapter implementing `IUserRepository` port | `@Repository class`     |

```kotlin
// Spring Data interface — no business logic
@Repository
interface MongoUserRepository : MongoRepository<UserDocument, String> {
    fun findByEmail(email: String): UserDocument?
}

// Port implementation — translates domain ↔ document
@Repository
class UserRepository(private val mongo: MongoUserRepository) : IUserRepository {
    override fun save(user: User): User = mongo.save(user.toDocument()).toDomain()
    override fun findByEmail(email: Email): User? = mongo.findByEmail(email.value)?.toDomain()
}
```

## Document design — `{Entity}Document`

Located in `adapter/secondary/persistence/document/`.

```kotlin
@Document(collection = "users")
data class UserDocument(
    @Id val id: String,
    @Field(name = "email") val email: String,
    @Field(name = "age") val age: Int,
    @Field(name = "password_hash") val passwordHash: String,
    @Field(name = "first_name") val firstName: String,
    @Field(name = "last_name") val lastName: String,
    @Field("created_at") val createdAt: Instant,
    @Field("updated_at") val updatedAt: Instant,
)
```

Rules:
- All fields `val`. `data class`.
- `@Field(name = "snake_case")` on every field — explicit Mongo field names, decoupled from Kotlin property names.
- `@Id` on the ID field (`String` UUID, not `ObjectId`).
- No domain types — all primitives or `String`/`Instant`/`Int`. Never `Email`, `Name`, etc.

## Persistence mapper

Located in `adapter/secondary/persistence/mapper/{Entity}PersistenceMapper.kt`. Extension functions only.

```kotlin
object UserPersistenceMapper {
    fun User.toDocument(): UserDocument = UserDocument(
        id = id.value,
        email = email.value,
        age = age.value,
        passwordHash = passwordHash.value,
        firstName = name.first,
        lastName = name.last,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun UserDocument.toDomain(): User = User(
        id = UserId(id),
        email = Email(email),
        age = Age(age),
        passwordHash = PasswordHash(passwordHash),
        name = Name(firstName, lastName),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
```

## Indexes

Simple unique field: `@Indexed(unique = true)` on the field.

Compound index on the class:

```kotlin
@Document(collection = "users")
@CompoundIndex(name = "email_created_idx", def = "{'email': 1, 'created_at': -1}")
data class UserDocument(...)
```

Ask user before adding indexes: unique? compound? partial? TTL?

## Query methods

Spring Data derives queries from method names. Use explicit `@Query` for complex cases:

```kotlin
@Query("{ 'email': ?0, 'active': true }")
fun findActiveByEmail(email: String): UserDocument?
```

Port interface accepts domain types. Adapter converts before calling Spring Data method.

## Rules

- `@Document` class lives in infra only. Never in domain or application.
- Port interface (`IUserRepository`) defined in `application/port/secondary/`. Adapter in infra.
- Mapper imports both domain types and `UserDocument`. Never in domain/application.
- Timestamps always `Instant` (UTC). No `LocalDateTime`.
- IDs: `String` UUID. Generate in domain (`UserId.generate()`), persist as String.

## Folder layout

```
adapter/secondary/persistence/
  document/
    UserDocument.kt
  mapper/
    UserPersistenceMapper.kt
  MongoUserRepository.kt    ← Spring Data interface
  UserRepository.kt         ← port adapter
```
