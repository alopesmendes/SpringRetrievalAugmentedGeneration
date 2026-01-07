package user.adapter.secondary.security

import com.ailtontech.user.adapter.secondary.security.BCryptPasswordHasherService
import com.ailtontech.user.port.secondary.IPasswordHasherService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [BCryptPasswordHasherService::class])
class BCryptPasswordHasherServiceTest {
    @Autowired
    private lateinit var passwordHasherService: IPasswordHasherService

    @Test
    fun bCryptPasswordHasherServiceIsPresentInContext() {
        assertThat(passwordHasherService).isNotNull
        assertThat(passwordHasherService).isInstanceOf(BCryptPasswordHasherService::class.java)
    }

    @Test
    fun canHashPasswordThroughInterface() {
        val rawPassword = "integration-test-password"
        val hash = passwordHasherService.hashPassword(rawPassword)

        assertThat(hash.value).startsWith("$2a$") // Standard BCrypt prefix
    }
}
