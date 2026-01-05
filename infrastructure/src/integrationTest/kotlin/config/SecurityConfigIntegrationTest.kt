package config

import com.ailtontech.config.SecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest
@ContextConfiguration(classes = [SecurityConfig::class])
class SecurityConfigIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun otherActuatorEndpointsReturnUnauthorizedWithoutAuth() {
        mockMvc
            .get("/actuator/metrics")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun protectedEndpointsReturnUnauthorizedWithoutAuth() {
        mockMvc
            .get("/api/v1/protected-resource")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun protectedEndpointsReturnOkWithValidAuth() {
        mockMvc
            .get("/api/v1/protected-resource") {
                with(httpBasic("user", "password"))
            }.andExpect {
                status { isUnauthorized() }
            }
    }
}
