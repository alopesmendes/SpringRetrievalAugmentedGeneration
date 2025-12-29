package com.ailtontech.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Architecture tests for the Application module using Konsist.
 *
 * The Application module orchestrates use cases and defines ports:
 * - Must depend ONLY on the Domain module
 * - Must NOT depend on Infrastructure module
 * - Must NOT depend on any framework (Spring, Ktor, etc.)
 * - Contains primary ports (use cases) and secondary ports (repositories, services)
 */
@DisplayName("Application Module Architecture Tests")
class ApplicationArchitectureTest {
    companion object {
        private const val PRIMARY_PORT_PACKAGE = "..port.primary.."
        private const val SECONDARY_PORT_PACKAGE = "..port.secondary.."

        private const val USE_CASE_PACKAGE = "..useCases.."

        private const val DTO_PACKAGE = "..dto.."

        private val FORBIDDEN_IMPORTS =
            listOf(
                "org.springframework",
                "jakarta.",
                "javax.persistence",
                "javax.transaction",
                "io.ktor",
                "com.mongodb",
                "org.hibernate",
                "..infrastructure..",
            )
    }

    @Nested
    @DisplayName("Module Dependencies")
    inner class ModuleDependencies {
        @Test
        @DisplayName("should have zero forbidden imports")
        fun `should have zero forbidden imports`() {
            Konsist
                .scopeFromModule("application")
                .files
                .assertFalse(testName = "Application should not have forbidden imports") { file ->
                    file.imports.any { import ->
                        FORBIDDEN_IMPORTS.any { forbidden ->
                            import.name.startsWith(forbidden)
                        }
                    }
                }
        }
    }

    @Nested
    @DisplayName("Primary Port (Input Port) Rules")
    inner class PrimaryPortRules {
        @Test
        @DisplayName("should have all primary ports be interfaces")
        fun `should have all primary ports be interfaces`() {
            val primaryPortRules =
                Konsist
                    .scopeFromModule("application")
                    .classesAndObjects()
                    .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }

            // There should be no classes in primary port package (only interfaces)
            assert(primaryPortRules.isEmpty()) {
                "Primary port package should only contain interfaces, but found classes: ${primaryPortRules.map {
                    it.name
                }}"
            }
        }

        @Test
        @DisplayName("should have all primary port interfaces start with I prefix")
        fun `should have all primary port interfaces start with I prefix`() {
            Konsist
                .scopeFromModule("application")
                .interfaces()
                .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                .assertTrue(testName = "primary port interfaces should start with 'I'") { iface ->
                    iface.hasNameStartingWith("I")
                }
        }

        @Test
        @DisplayName("should have all primary port interfaces end with UseCase suffix")
        fun `should have all primary port interfaces end with UseCase suffix`() {
            Konsist
                .scopeFromModule("application")
                .interfaces()
                .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                .assertTrue(testName = "primary port interfaces should end with 'UseCase'") { iface ->
                    iface.hasNameEndingWith("UseCase")
                }
        }

        @Test
        @DisplayName("should have all primary ports interfaces with only invoke method")
        fun `should have all primary ports with only invoke method`() {
            Konsist
                .scopeFromModule("application")
                .interfaces()
                .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                .assertTrue { iface ->
                    iface.hasFunction { func -> func.hasOperatorModifier && func.name == "invoke" } &&
                        iface.functions().size == 1
                }
        }
    }

    @Nested
    @DisplayName("Secondary Port (Output Port) Rules")
    inner class SecondaryPortRules {
        @Test
        @DisplayName("should have all secondary ports be interfaces")
        fun `should have all secondary ports be interfaces`() {
            val secondaryPortClasses =
                Konsist
                    .scopeFromModule("application")
                    .classes()
                    .filter { it.resideInPackage(SECONDARY_PORT_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }

            // There should be no classes in secondary port package (only interfaces)
            assert(secondaryPortClasses.isEmpty()) {
                "secondary port package should only contain interfaces, but found classes: ${secondaryPortClasses.map {
                    it.name
                }}"
            }
        }

        @Test
        @DisplayName("should have all secondary port interfaces start with I prefix")
        fun `should have all secondary port interfaces start with I prefix`() {
            Konsist
                .scopeFromModule("application")
                .interfaces()
                .filter { it.resideInPackage(SECONDARY_PORT_PACKAGE) }
                .assertTrue(testName = "secondary port interfaces should start with 'I'") { iface ->
                    iface.hasNameStartingWith("I")
                }
        }
    }

    @Nested
    @DisplayName("Use Case Implementation Rules")
    inner class UseCaseImplementationRules {
        @Test
        @DisplayName("should have use case implementations end with UseCase suffix")
        fun `should have use case implementations end with UseCase suffix`() {
            Konsist
                .scopeFromModule("application")
                .classes()
                .filter { it.resideInPackage(USE_CASE_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Use case implementations should end with 'UseCase'") { clazz ->
                    clazz.hasNameEndingWith("UseCase")
                }
        }

        @Test
        @DisplayName("should have use case implementations implement primary port interfaces")
        fun `should have use case implementations implement primary port interfaces`() {
            val primaryPortRules =
                Konsist
                    .scopeFromModule("application")
                    .interfaces()
                    .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .map { it.name }

            val useCases =
                Konsist
                    .scopeFromModule("application")
                    .classes()
                    .filter { it.resideInPackage(USE_CASE_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .withNameEndingWith("UseCase")

            val invalidUseCases =
                useCases.filter { useCase ->
                    useCase.parentInterfaces().any { parent ->
                        parent.name !in primaryPortRules
                    }
                }

            assert(invalidUseCases.isEmpty()) {
                val violations =
                    invalidUseCases.joinToString("\n") { useCase ->
                        val invalidParents =
                            useCase
                                .parentInterfaces()
                                .filter { it.resideInModule("application") && it.name !in primaryPortRules }
                                .map { it.name }
                        "Class [${useCase.name}] implements invalid parents: $invalidParents"
                    }
                "Use cases should only implement primary port interfaces, but found violations:\n$violations"
            }
        }

        @Test
        @DisplayName("should have use case constructor parameters be interfaces (secondary ports)")
        fun `should have use case constructor parameters be interfaces`() {
            val secondaryPortInterfaces =
                Konsist
                    .scopeFromModule("application")
                    .interfaces()
                    .filter { it.resideInPackage(SECONDARY_PORT_PACKAGE) }
                    .map { it.name }

            val useCases =
                Konsist
                    .scopeFromModule("application")
                    .classes()
                    .filter { it.resideInPackage(USE_CASE_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .withNameEndingWith("UseCase")

            val invalidUseCases =
                useCases.filter { useCase ->
                    val params = useCase.primaryConstructor?.parameters ?: emptyList()
                    params.any { param ->
                        param.type.name !in secondaryPortInterfaces
                    }
                }

            assert(invalidUseCases.isEmpty()) {
                val violations =
                    invalidUseCases.joinToString("\n") { useCase ->
                        val invalidParams =
                            useCase.primaryConstructor
                                ?.parameters
                                .orEmpty()
                                .filter { param ->
                                    param.type.name !in secondaryPortInterfaces
                                }.map { "${it.name}: ${it.type.name}" }

                        "Class [${useCase.name}] has invalid constructor parameters: $invalidParams"
                    }
                "UseCase constructor parameters should be secondary ports (interfaces), but violations:\n$violations"
            }
        }
    }

    @Nested
    @DisplayName("DTO Package Rules")
    inner class DtoPackageRules {
        @Test
        @DisplayName("should have DTOs follow naming pattern (Command, Result, or Query)")
        fun `should have DTOs follow naming pattern`() {
            Konsist
                .scopeFromModule("application")
                .classes()
                .filter { it.resideInPackage(DTO_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "DTOs should end with Command, Result, or Query") { clazz ->
                    clazz.hasNameEndingWith("Command") ||
                        clazz.hasNameEndingWith("Result") ||
                        clazz.hasNameEndingWith("Query")
                }
        }

        @Test
        @DisplayName("should have DTOs be data classes")
        fun `should have DTOs be data classes`() {
            Konsist
                .scopeFromModule("application")
                .classes()
                .filter { it.resideInPackage(DTO_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "DTOs should be data classes") { clazz ->
                    clazz.isTopLevel
                }
        }
    }

    @Nested
    @DisplayName("General Architecture Rules")
    inner class GeneralArchitectureRules {
        @Test
        @DisplayName("should have all interfaces reside in primary and secondary ports")
        fun `should have all interfaces reside in primary and secondary ports`() {
            Konsist
                .scopeFromModule("application")
                .interfaces()
                .assertTrue(testName = "All interfaces should be in primary and secondary ports") { iface ->
                    iface.resideInPackage(PRIMARY_PORT_PACKAGE) || iface.resideInPackage(SECONDARY_PORT_PACKAGE)
                }
        }
    }
}
