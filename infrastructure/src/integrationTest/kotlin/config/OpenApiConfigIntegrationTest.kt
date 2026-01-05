package config

import com.ailtontech.config.OpenApiConfig
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [OpenApiConfig::class])
@TestPropertySource(
    properties = [
        "spring.application.name=test-image-rag",
        "spring.profiles.active=test",
    ],
)
class OpenApiConfigIntegrationTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun openApiBeanIsPresentInContext() {
        val bean = context.getBean(OpenAPI::class.java)

        assertThat(bean).isNotNull
        assertThat(bean.info.title).isEqualTo("TEST-IMAGE-RAG API")
    }

    @Test
    fun openApiConfigurationHasCorrectMetadata() {
        val openApi = context.getBean(OpenAPI::class.java)
        val info = openApi.info

        assertThat(info.version).isEqualTo("1.0.0")
        assertThat(info.description).contains("Image Retrieval Augmented Generation")
        assertThat(info.contact.name).isEqualTo("Ailton Tech")
        assertThat(info.license.name).isEqualTo("MIT License")
    }

    @Test
    fun openApiHasBearerAuthSecuritySchemeDefined() {
        val openApi = context.getBean(OpenAPI::class.java)
        val securitySchemes = openApi.components.securitySchemes
        val securitySchemeName = "bearerAuth"

        assertThat(securitySchemes).containsKey(securitySchemeName)

        val bearerAuth = securitySchemes[securitySchemeName]
        assertThat(bearerAuth?.type).isEqualTo(SecurityScheme.Type.HTTP)
        assertThat(bearerAuth?.scheme).isEqualTo("bearer")
        assertThat(bearerAuth?.bearerFormat).isEqualTo("JWT")
    }

    @Test
    fun openApiServerUrlReflectsActiveProfile() {
        val openApi = context.getBean(OpenAPI::class.java)
        val server = openApi.servers.firstOrNull()

        assertThat(server).isNotNull
        assertThat(server?.description).isEqualTo("Current Environment: test")
    }
}
