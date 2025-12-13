import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

// ============================================================================
// Ktlint Configuration (Code Style & Formatting)
// ============================================================================
ktlint {
    version.set("1.5.0")
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// ============================================================================
// Detekt Configuration (Static Code Analysis)
// ============================================================================
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
    parallel = true
    ignoreFailures = false
    autoCorrect = false
}

allprojects {
    group = "com.ailtontech"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// ============================================================================
// Subprojects Configuration
// ============================================================================
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    // Ktlint configuration for subprojects
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)

        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    // Detekt configuration for subprojects
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        baseline = file("$rootDir/config/detekt/baseline.xml")
        parallel = true
        ignoreFailures = false
        autoCorrect = false
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            html.required.set(true)
            xml.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }
}

// ============================================================================
// Custom Tasks
// ============================================================================

// Task to run all linters
tasks.register("lint") {
    group = "verification"
    description = "Run all linters (ktlint and detekt)"
    dependsOn("ktlintCheck", "detekt")
}

// Task to format code
tasks.register("format") {
    group = "formatting"
    description = "Format code with ktlint"
    dependsOn("ktlintFormat")
}
