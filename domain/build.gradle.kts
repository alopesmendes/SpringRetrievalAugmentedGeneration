plugins {
    kotlin("jvm")
}

description = "Domain layer - Pure Kotlin business logic with zero framework dependencies"

dependencies {
    // Kotlin standard library only - NO SPRING, NO MONGODB, NO FRAMEWORK DEPENDENCIES
    implementation(kotlin("stdlib"))

    // Testing - Pure Kotlin tests
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.12")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
