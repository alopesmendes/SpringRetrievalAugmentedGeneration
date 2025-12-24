package com.ailtontech.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.FileSystemResource

/**
 * Application configuration that handles environment variable loading.
 *
 * Configuration priority:
 * 1. System environment variables (from Render/GitHub Actions in deployment)
 * 2. Local env files (env/.env.{APP_ENVIRONMENT}) for development
 *
 * In deployment (Render):
 * - Environment variables are injected directly by the platform
 * - No env files are needed or expected
 *
 * In local development:
 * - Uses env/.env.{environment} files
 * - APP_ENVIRONMENT defaults to "development" if not set
 */
@Configuration
class AppConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(AppConfig::class.java)

        private const val DEFAULT_ENVIRONMENT = "development"
        private const val ENV_FILE_PREFIX = "env/.env."

        @Bean
        @JvmStatic
        fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
            val configurer = PropertySourcesPlaceholderConfigurer()
            val targetEnv = System.getenv("APP_ENVIRONMENT") ?: DEFAULT_ENVIRONMENT

            // Check if we're in a deployment environment (Render/GitHub Actions)
            // Render sets PORT, GitHub Actions sets CI
            val isDeployment = isDeploymentEnvironment()

            if (isDeployment) {
                logger.info("Running in deployment mode - using system environment variables")
                logger.info("APP_ENVIRONMENT: $targetEnv")
                // In deployment, system environment variables are already available
                // No need to load from file
                return configurer
            }

            // Local development - try to load from env file
            val envFile = FileSystemResource("$ENV_FILE_PREFIX$targetEnv")

            if (envFile.exists()) {
                logger.info("Loading configuration from: ${envFile.path}")
                configurer.setLocation(envFile)
            } else {
                logger.warn("Environment file not found: ${envFile.path}")
                logger.warn("Falling back to system environment variables")
                // Don't throw - allow fallback to system environment variables
                // This enables running locally with exported env vars if preferred
            }

            return configurer
        }

        /**
         * Detects if the application is running in a deployment environment.
         *
         * Checks for common deployment indicators:
         * - RENDER: Set by Render platform
         * - CI: Set by GitHub Actions
         * - KUBERNETES_SERVICE_HOST: Set in Kubernetes
         * - DYNO: Set by Heroku
         */
        private fun isDeploymentEnvironment(): Boolean {
            val deploymentIndicators =
                listOf(
                    "RENDER", // Render platform
                    "RENDER_SERVICE_ID", // Render service
                    "CI", // GitHub Actions / CI systems
                    "KUBERNETES_SERVICE_HOST", // Kubernetes
                    "DYNO", // Heroku
                    "AWS_EXECUTION_ENV", // AWS Lambda
                    "GOOGLE_CLOUD_PROJECT", // Google Cloud
                )

            return deploymentIndicators.any { indicator ->
                System.getenv(indicator) != null
            }
        }
    }
}
