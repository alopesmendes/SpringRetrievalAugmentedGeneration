package user.adapter.secondary.persistence

import AbstractMongoIntegrationTest
import com.ailtontech.user.adapter.secondary.persistence.MongoUserRepository
import com.ailtontech.user.adapter.secondary.persistence.UserRepository
import com.ailtontech.user.entity.User
import com.ailtontech.user.valueobjects.Age
import com.ailtontech.user.valueobjects.Email
import com.ailtontech.user.valueobjects.Name
import com.ailtontech.user.valueobjects.PasswordHash
import com.ailtontech.user.valueobjects.UserId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryIntegrationTest : AbstractMongoIntegrationTest() {
    @Autowired
    private lateinit var mongoUserRepository: MongoUserRepository

    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository = UserRepository(mongoUserRepository)
    }

    @AfterEach
    fun tearDown() {
        mongoUserRepository.deleteAll()
    }

    @Test
    fun saveUserPersistsDocumentInMongoDB() {
        val user = createTestUser()

        val savedUser = userRepository.save(user)

        assertEquals(user.id, savedUser.id)
        assertEquals(user.email, savedUser.email)
    }

    @Test
    fun findByIdReturnsUserWhenExists() {
        val user = createTestUser()
        userRepository.save(user)

        val foundUser = userRepository.findById(user.id)

        assertNotNull(foundUser)
        assertEquals(user.id, foundUser.id)
        assertEquals(user.email, foundUser.email)
    }

    @Test
    fun findByIdReturnsNullWhenNotExists() {
        val nonExistentId = UserId.generate()

        val foundUser = userRepository.findById(nonExistentId)

        assertNull(foundUser)
    }

    @Test
    fun existsByEmailReturnsTrueWhenUserExists() {
        val user = createTestUser()
        userRepository.save(user)

        val exists = userRepository.existsByEmail(user.email)

        assertEquals(true, exists)
    }

    @Test
    fun existsByEmailReturnsFalseWhenUserNotExists() {
        val email = Email.from("nonexistent@test.com")

        val exists = userRepository.existsByEmail(email)

        assertEquals(false, exists)
    }

    private fun createTestUser(): User = User.create(
        firstName = Name.from("Jane"),
        lastName = Name.from("Doe"),
        age = Age.from(25),
        email = Email.from("jane@doe.com"),
        password = PasswordHash.from("password_hash"),
    )
}
