package com.ailtontech.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.FileSystemResource

@Configuration
class AppConfig {
    companion object {
        @Bean
        @JvmStatic
        fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
            val configurer = PropertySourcesPlaceholderConfigurer()
            val targetEnv = System.getenv("APP_ENVIRONMENT") ?: "development"
            val envFile = FileSystemResource("env/.env.$targetEnv")
            if (envFile.exists()) {
                configurer.setLocation(envFile)
            } else {
                println("WARNING: Could not find environment file: ${envFile.path}")
                throw IllegalArgumentException("Environment file does not exist with given target: ${envFile.path}")
            }

            return configurer
        }
    }
}
