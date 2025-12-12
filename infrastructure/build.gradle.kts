plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

description = "Infrastructure layer - Spring Boot adapters and framework integrations"

// For spring AI, for now since there's no key will comment it
/*
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.2")
    }
}
*/

dependencies {
    // Internal module dependencies
    implementation(project(":domain"))
    implementation(project(":application"))

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // For now ignore mongodb also
    // implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring AI (OpenAI)
    // implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // OpenAPI Documentation (Swagger)
    // implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")
    testImplementation("org.springframework.security:spring-security-test")

    // Testcontainers for MongoDB
    testImplementation("org.testcontainers:mongodb:2.0.2")
    testImplementation("org.testcontainers:junit-jupiter:2.0.2")

    // ArchUnit for architecture testing
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("com.ailtontech.imagerag.ImageRagApplicationKt")
}