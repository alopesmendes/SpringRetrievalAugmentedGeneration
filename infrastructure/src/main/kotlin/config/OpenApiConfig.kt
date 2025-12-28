package com.ailtontech.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Value("\${spring.application.name:image-rag}")
    private lateinit var applicationName: String

    @Value("\${spring.profiles.active:development}")
    private lateinit var activeProfile: String

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("$applicationName API")
                .description(
                    "API for Image Retrieval Augmented Generation - Extract and store information from images using AI",
                ).contact(
                    Contact()
                        .name("Ailton Tech")
                        .url("https://github.com/alopesmendes/SpringRetrievalAugmentedGeneration"),
                ).license(
                    License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"),
                ),
        ).servers(
            listOf(
                Server()
                    .url("/")
                    .description("Current Environment: $activeProfile"),
            ),
        ).components(
            Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token"),
                ),
        ).addSecurityItem(
            SecurityRequirement().addList("bearerAuth"),
        )
}
