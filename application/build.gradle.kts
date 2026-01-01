plugins {
    kotlin("jvm")
}

description = "Application layer - Use cases and ports with zero framework dependencies"

dependencies {
    // Internal module dependencies
    implementation(project(":domain"))

    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // Test domain module as well
    testImplementation(project(":domain"))
}
