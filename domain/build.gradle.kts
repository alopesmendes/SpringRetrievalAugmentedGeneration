plugins {
    kotlin("jvm")
}

description = "Domain layer - Pure Kotlin business logic with zero framework dependencies"

dependencies {
    // Kotlin standard library only - NO SPRING, NO MONGODB, NO FRAMEWORK DEPENDENCIES
    implementation(kotlin("stdlib"))

    // Testing - Pure Kotlin tests
}
