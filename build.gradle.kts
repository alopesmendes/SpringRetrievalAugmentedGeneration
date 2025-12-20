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
// Helper Functions
// ============================================================================

// Check if test results exist for a subproject
fun hasTestResults(subproject: Project): Boolean {
    val testResultsDir = file("${subproject.projectDir}/build/test-results/test")
    return testResultsDir.exists() && testResultsDir.listFiles()?.any { it.extension == "xml" } == true
}

// Check if any test results exist across all subprojects
fun hasAnyTestResults(): Boolean = subprojects.any { hasTestResults(it) }

// Extract line coverage percentage from Kover XML report
fun extractCoverageFromXml(xmlFile: File): Double = try {
    val content = xmlFile.readText()
    // Kover XML format: <counter type="LINE" missed="X" covered="Y"/>
    val lineCounterRegex = """<counter type="LINE" missed="(\d+)" covered="(\d+)"/>""".toRegex()
    val match = lineCounterRegex.find(content)
    if (match != null) {
        val missed = match.groupValues[1].toDouble()
        val covered = match.groupValues[2].toDouble()
        val total = missed + covered
        if (total > 0) (covered / total) * 100 else 0.0
    } else {
        0.0
    }
} catch (_: Exception) {
    0.0
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

// ============================================================================
// Coverage Tasks - Kover integration (smart: skips tests if results exist)
// ============================================================================

// Task to run tests only for modules missing test results
tasks.register("ensureTestResults") {
    group = "verification"
    description = "Run tests only for modules that don't have existing test results"

    val modulesWithResults = mutableListOf<String>()
    val modulesWithoutResults = mutableListOf<String>()

    doFirst {
        subprojects.forEach { subproject ->
            if (hasTestResults(subproject)) {
                modulesWithResults.add(subproject.path)
            } else {
                modulesWithoutResults.add(subproject.path)
            }
        }

        if (modulesWithResults.isNotEmpty()) {
            println("✅ Test results found for: ${modulesWithResults.joinToString(", ")}")
        }
        if (modulesWithoutResults.isNotEmpty()) {
            println("🧪 Running tests for: ${modulesWithoutResults.joinToString(", ")}")
        }
        if (modulesWithoutResults.isEmpty()) {
            println("✅ All modules have existing test results - skipping test execution")
        }
    }

    // Dynamically depend on test tasks only for modules without results
    modulesWithoutResults.forEach {
        dependsOn("$it:test")
    }
}

// Task to generate coverage reports (runs tests only if needed)
tasks.register("coverage") {
    group = "verification"
    description = "Generate coverage reports for all modules (skips tests if results exist)"

    dependsOn("ensureTestResults")

    // Generate Kover reports for each subproject
    dependsOn(subprojects.map { "${it.path}:koverXmlReport" })
    dependsOn(subprojects.map { "${it.path}:koverHtmlReport" })
}

// Task to verify coverage meets the threshold (80%)
tasks.register("verifyCoverage") {
    group = "verification"
    description = "Verify global coverage meets the 80% minimum threshold"

    dependsOn("coverage")
    dependsOn("koverVerify")
}

// Task to assemble all coverage reports into a unified directory
tasks.register<Copy>("assembleCoverageReports") {
    group = "verification"
    description = "Assemble all coverage reports (XML and HTML) into a unified directory"

    val outputDir = layout.buildDirectory.dir("coverage-reports")

    // Collect XML reports from all subprojects
    subprojects.forEach { subproject ->
        from("${subproject.projectDir}/build/reports/kover/report.xml") {
            rename { "${subproject.name}-coverage.xml" }
            into("xml")
        }
        from("${subproject.projectDir}/build/reports/kover/html") {
            into("html/${subproject.name}")
        }
    }

    into(outputDir)

    includeEmptyDirs = false
}

// Task to print coverage summary to console
tasks.register("printCoverageSummary") {
    group = "verification"
    description = "Print coverage summary for all modules"

    doLast {
        println()
        println("=".repeat(70))
        println("📊 CODE COVERAGE SUMMARY")
        println("=".repeat(70))
        println()

        var allModulesAboveThreshold = true

        // Parse and display coverage for each module
        subprojects.forEach { subproject ->
            val reportFile = file("${subproject.projectDir}/build/reports/kover/report.xml")
            if (reportFile.exists()) {
                val coverage = extractCoverageFromXml(reportFile)
                val status = if (coverage >= 80) "✅" else "⚠️"
                if (coverage < 80) allModulesAboveThreshold = false
                println("  $status ${subproject.name.padEnd(20)} : ${String.format("%6.2f", coverage)}%")
            } else {
                println("  ⏭️  ${subproject.name.padEnd(20)} : No coverage report found")
            }
        }

        println()
        println("-".repeat(70))

        // Global coverage
        val globalReportFile = file("build/reports/kover/report.xml")
        if (globalReportFile.exists()) {
            val globalCoverage = extractCoverageFromXml(globalReportFile)
            val globalStatus = if (globalCoverage >= 80) "✅" else "❌"
            println("  $globalStatus ${"GLOBAL".padEnd(20)} : ${String.format("%6.2f", globalCoverage)}%")
            println()
            println("-".repeat(70))

            if (globalCoverage >= 80) {
                println("✅ Global coverage meets the 80% minimum threshold")
            } else {
                println("❌ Global coverage is below the 80% minimum threshold")
            }
        } else {
            println("  ⏭️  GLOBAL                : No coverage report found")
        }

        println("=".repeat(70))
        println()
    }
}
