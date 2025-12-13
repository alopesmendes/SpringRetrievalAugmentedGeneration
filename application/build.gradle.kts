plugins {
    kotlin("jvm")
}

description = "Application layer - Use cases and ports with zero framework dependencies"

dependencies {
    // Internal module dependencies
    implementation(project(":domain"))

    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.12")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Test domain module as well
    testImplementation(project(":domain"))
}
