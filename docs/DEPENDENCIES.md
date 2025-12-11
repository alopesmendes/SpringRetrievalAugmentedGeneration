# Dependencies

## Build Configuration (Gradle Kotlin DSL)

```kotlin
plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

---

## Core Dependencies

### Spring Boot Starters

```kotlin
dependencies {
    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

### Kotlin

```kotlin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

---

## Spring AI

```kotlin
dependencies {
    implementation(platform("org.springframework.ai:spring-ai-bom:1.1.1"))
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    // Or other providers: anthropic, bedrock, ollama, etc.
}
```

---

## API Documentation (OpenAPI/Swagger)

```kotlin
dependencies {
    // For Spring Boot 4.x
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
}
```

---

## Testing

```kotlin
dependencies {
    // JUnit 5
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // Mockito for Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    
    // Security testing
    testImplementation("org.springframework.security:spring-security-test")
    
    // Testcontainers for MongoDB
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    
    // ArchUnit for architecture tests
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
}
```

---

## Complete build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ailtontech"
version = "0.0.1-SNAPSHOT"
description = "Generate information from an image and store it"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.1.1")
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Spring AI
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    
    // OpenAPI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

---

## Version Summary

| Dependency               | Version   | Purpose                          |
|--------------------------|-----------|----------------------------------|
| Kotlin                   | 2.2.21    | Language                         |
| Spring Boot              | 4.0.0     | Framework                        |
| Spring AI                | 1.1.1     | AI/Image analysis                |
| Spring Security          | 7.0.x     | Authentication/Authorization     |
| Spring Data MongoDB      | 2025.1.x  | Database                         |
| springdoc-openapi        | 3.0.0     | API documentation                |
| Mockito Kotlin           | 5.4.0     | Mocking in tests                 |
| Testcontainers           | 1.20.4    | Integration testing              |
| ArchUnit                 | 1.4.0     | Architecture testing             |

---

## Notes

- Spring Boot 4.0.0 requires Java 17+ and uses Spring Framework 7.0
- Spring AI 1.1.1 supports multiple AI providers (OpenAI, Anthropic, Bedrock, Ollama)
- springdoc-openapi 3.0.0 is required for Spring Boot 4.x compatibility
- Testcontainers requires Docker to run integration tests
