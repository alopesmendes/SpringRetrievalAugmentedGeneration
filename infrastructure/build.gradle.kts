plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}

description = "Infrastructure layer - Spring Boot adapters and framework integrations"

// For spring AI, for now since there's no key will comment it
// dependencyManagement {
//    imports {
//        mavenBom("org.springframework.ai:spring-ai-bom:1.0.2")
//    }
// }

// =============================================================================
// Source Sets Configuration
// =============================================================================

val integrationTest: SourceSet by sourceSets.creating {
    kotlin.srcDir("src/integrationTest/kotlin")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}

val e2eTest: SourceSet by sourceSets.creating {
    kotlin.srcDir("src/e2eTest/kotlin")
    resources.srcDir("src/e2eTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}

// =============================================================================
// Configurations - Extend test configurations
// =============================================================================

configurations {
    named("integrationTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("integrationTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
    //
    named("e2eTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("e2eTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

// =============================================================================
// Dependencies
// =============================================================================

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

    // -------------------------------------------------------------------------
    // Testing (Unit Tests)
    // -------------------------------------------------------------------------
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    // SpringMockK: Allows using @MockkBean inside Spring Contexts
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("org.springframework.security:spring-security-test")
    // ArchUnit for architecture testing
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // -------------------------------------------------------------------------
    // Integration Testing
    // -------------------------------------------------------------------------
    // Testcontainers for MongoDB (uncomment when needed)
    // "integrationTestImplementation"("org.testcontainers:mongodb:2.0.2")
    // "integrationTestImplementation"("org.testcontainers:junit-jupiter:2.0.2")

    // -------------------------------------------------------------------------
    // E2E Testing (Cucumber + JUnit 5)
    // -------------------------------------------------------------------------
    val cucumberVersion = "7.33.0"
    "e2eTestImplementation"("io.cucumber:cucumber-java:$cucumberVersion")
    "e2eTestImplementation"("io.cucumber:cucumber-spring:$cucumberVersion")
    "e2eTestImplementation"("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    // To run Cucumber as a JUnit 5 Suite
    "e2eTestImplementation"("org.junit.platform:junit-platform-suite:6.0.1")
}

// =============================================================================
// Test Tasks Configuration
// =============================================================================

// Unit Tests (default test task)
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

// Integration Tests
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()
    shouldRunAfter(tasks.test)

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

// E2E Tests (Cucumber)
tasks.register<Test>("e2eTest") {
    description = "Runs E2E tests with Cucumber"
    group = "verification"

    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath

    useJUnitPlatform()
    shouldRunAfter(tasks.named("integrationTest"))

    // Cucumber configuration
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    systemProperty("cucumber.plugin", "pretty,html:build/reports/cucumber/cucumber.html")
    systemProperty("cucumber.publish.quiet", "true")

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

// Make check depend on all test types
tasks.named("check") {
    dependsOn("integrationTest", "e2eTest")
}

// =============================================================================
// Spring Boot Configuration
// =============================================================================

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("com.ailtontech.ImageRetrievalAugmentedGenerationKt")
}
