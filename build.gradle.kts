import io.gitlab.arturbosch.detekt.Detekt
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
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

// ============================================================================
// Kover Configuration
// ============================================================================
kover {
    reports {
        // Global report combining all modules
        total {
            xml { onCheck = true }
            html { onCheck = true }

            verify {
                onCheck = true
                description = "Global Line Coverage"

                rule {
                    bound {
                        minValue = 80
                        coverageUnits = CoverageUnit.LINE
                        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                    }
                }
            }
        }
    }

    // Exclude from coverage measurement
    currentProject {
        sources {
            excludedSourceSets.addAll("integrationTest", "e2eTest")
        }
    }
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
    apply(plugin = "org.jetbrains.kotlinx.kover")

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

// ============================================================================
// Report Merging (Fix for GitHub Code Scanning)
// ============================================================================
tasks.register<io.gitlab.arturbosch.detekt.report.ReportMergeTask>("mergeDetektSarif") {
    group = "verification"
    description = "Merges Detekt SARIF reports from all submodules into one file."

    // 1. Define where the merged file will be saved
    output.set(layout.buildDirectory.file("reports/detekt/merged.sarif"))

    // 2. Collect the 'detekt.sarif' file from every subproject
    input.from(
        subprojects.map { project ->
            project.layout.buildDirectory.file("reports/detekt/detekt.sarif")
        },
    )

    // Ensure this task runs after the subprojects have generated their reports
    dependsOn(subprojects.map { it.tasks.named("detekt") })
}

// ============================================================================
// Test Tasks - Consolidated across all modules
// ============================================================================

// Task to run unit tests for all subprojects
tasks.register("unitTest") {
    group = "verification"
    description = "Run unit tests for all modules"
    dependsOn(subprojects.map { "${it.path}:test" })
}

// Task to run integration tests
tasks.register("integrationTest") {
    group = "verification"
    description = "Run integration tests for infrastructure module"
    dependsOn(":infrastructure:integrationTest")
}

// Task to run E2E tests
tasks.register("e2eTest") {
    group = "verification"
    description = "Run E2E tests with Cucumber"
    dependsOn(":infrastructure:e2eTest")
}

// ============================================================================
// Test Results Assembly - Single task to collect all test results
// ============================================================================

// Task to assemble all test results into a unified directory for CI/coverage
// Results are saved with naming pattern: {module}-{testType}-{originalFileName}.xml
tasks.register<Copy>("assembleTestResults") {
    group = "verification"
    description = "Assemble all test results (unit, integration, e2e) into a unified directory"

    val outputDir = layout.buildDirectory.dir("test-results")

    // Collect unit test results from all subprojects
    subprojects.forEach { subproject ->
        from("${subproject.projectDir}/build/test-results/test") {
            include("**/*.xml")
            rename { fileName -> "${subproject.name}-unit-$fileName" }
        }
    }

    // Collect integration test results from infrastructure
    from("infrastructure/build/test-results/integrationTest") {
        include("**/*.xml")
        rename { fileName -> "infrastructure-integration-$fileName" }
    }

    // Collect E2E test results from infrastructure
    from("infrastructure/build/test-results/e2eTest") {
        include("**/*.xml")
        rename { fileName -> "infrastructure-e2e-$fileName" }
    }

    into(outputDir)

    // Flatten directory structure - all files go directly into output dir
    eachFile {
        relativePath = RelativePath(true, name)
    }

    includeEmptyDirs = false
}

// ============================================================================
// Test Reports Assembly - Single task to collect all test reports
// ============================================================================

// Task to assemble all test reports into a unified directory
tasks.register<Copy>("assembleTestReports") {
    group = "verification"
    description = "Assemble all test reports (unit, integration, e2e) into a unified directory"

    val outputDir = layout.buildDirectory.dir("reports/tests")

    // Collect unit test reports from all subprojects
    subprojects.forEach { subproject ->
        from("${subproject.projectDir}/build/reports/tests/test") {
            into("${subproject.name}-unit")
        }
    }

    // Collect integration test reports from infrastructure
    from("infrastructure/build/reports/tests/integrationTest") {
        into("infrastructure-integration")
    }

    // Collect E2E test reports from infrastructure
    from("infrastructure/build/reports/tests/e2eTest") {
        into("infrastructure-e2e")
    }

    // Collect Cucumber reports
    from("infrastructure/build/reports/cucumber") {
        into("infrastructure-cucumber")
    }

    into(outputDir)

    includeEmptyDirs = false
}

// Combined task to assemble both results and reports
tasks.register("assembleTestArtifacts") {
    group = "verification"
    description = "Assemble all test results and reports for CI"
    dependsOn("assembleTestResults", "assembleTestReports")
}
