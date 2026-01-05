package user.config

import com.ailtontech.user.config.UserBeanConfig
import com.ailtontech.user.port.primary.ICreateUserUseCase
import com.ailtontech.user.port.primary.IGetUserUseCase
import com.ailtontech.user.port.primary.IUpdateUserUseCase
import com.ailtontech.user.port.secondary.IPasswordHasherService
import com.ailtontech.user.port.secondary.IUserRepository
import com.ailtontech.user.useCases.CreateUserUseCase
import com.ailtontech.user.useCases.GetUserUseCase
import com.ailtontech.user.useCases.UpdateUserUseCase
import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest(classes = [UserBeanConfig::class])
class UserBeanConfigIntegrationTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @MockkBean
    private lateinit var userRepository: IUserRepository

    @MockkBean
    private lateinit var passwordHasher: IPasswordHasherService

    @Test
    fun createUserUseCaseBeanIsPresentInContext() {
        val bean = context.getBean(ICreateUserUseCase::class.java)

        assertThat(bean).isNotNull
        assertThat(bean).isInstanceOf(CreateUserUseCase::class.java)
    }

    @Test
    fun getUserUseCaseBeanIsPresentInContext() {
        val bean = context.getBean(IGetUserUseCase::class.java)

        assertThat(bean).isNotNull
        assertThat(bean).isInstanceOf(GetUserUseCase::class.java)
    }

    @Test
    fun updateUserUseCaseBeanIsPresentInContext() {
        val bean = context.getBean(IUpdateUserUseCase::class.java)

        assertThat(bean).isNotNull
        assertThat(bean).isInstanceOf(UpdateUserUseCase::class.java)
    }

    @Test
    fun passwordServiceBeanIsPresentInContext() {
        val bean = context.containsBean("passwordService")

        assertThat(bean).isTrue()
    }
}
