package error.rest

import com.ailtontech.error.rest.GlobalExceptionHandler
import com.ailtontech.user.exception.UserAlreadyExistsException
import com.ailtontech.user.exception.UserNotFoundException
import com.ailtontech.user.valueobjects.Email
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private val email = Email.from("jane@doe.com")

/**
 * Fake controller created specifically to trigger exceptions handled by GlobalExceptionHandler.
 */
@RestController
class TestController {
    @GetMapping("/test/not-found")
    fun throwNotFound(): Unit = throw UserNotFoundException("user_id")

    @GetMapping("/test/conflict")
    fun throwConflict(): Unit = throw UserAlreadyExistsException(email = email)
}

/**
 * Integration tests for [GlobalExceptionHandler].
 *
 * Uses standalone MockMvc setup which:
 * - Does not require Spring Boot test context
 * - Explicitly configures the controller and exception handler
 * - Avoids security configuration issues
 * - Runs faster than context-based tests
 */
class GlobalExceptionHandlerIntegrationTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(TestController())
                .setControllerAdvice(GlobalExceptionHandler())
                .build()
    }

    @Test
    fun userNotFoundExceptionReturns404WithErrorPayload() {
        mockMvc
            .perform(
                get("/test/not-found")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Not found user with this user_id"))
            .andExpect(jsonPath("$.path").value("/test/not-found"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun userAlreadyExistsExceptionReturns409WithErrorPayload() {
        mockMvc
            .perform(
                get("/test/conflict")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("The user already exists at this address $email"))
            .andExpect(jsonPath("$.path").value("/test/conflict"))
    }
}
