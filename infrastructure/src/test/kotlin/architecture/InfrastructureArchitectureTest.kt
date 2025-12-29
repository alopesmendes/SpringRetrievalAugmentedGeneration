package com.ailtontech.architecture

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.sequences.filter

/**
 * Architecture tests for the Infrastructure module using Konsist.
 *
 * The Infrastructure module contains all framework-specific implementations:
 * - Primary adapters (REST controllers, etc.)
 * - Secondary adapters (repositories, external service clients)
 * - Configuration classes
 *
 * This module CAN depend on Application and Domain modules.
 */
@DisplayName("Infrastructure Module Architecture Tests")
class InfrastructureArchitectureTest {
    companion object {
        // Port packages
        private const val PRIMARY_PORT_PACKAGE = "..port.primary.."
        private const val SECONDARY_PORT_PACKAGE = "..port.secondary.."

        // Adapter packages
        private const val PRIMARY_ADAPTER_PACKAGE = "..adapter.primary"
        private const val SECONDARY_ADAPTER_PACKAGE = "..adapter.secondary"

        // REST adapter packages
        private const val REST_PACKAGE = "$PRIMARY_ADAPTER_PACKAGE.rest.."
        private const val REST_DTO_PACKAGE = "$PRIMARY_ADAPTER_PACKAGE.rest.dto.."
        private const val REST_MAPPER_PACKAGE = "$PRIMARY_ADAPTER_PACKAGE.rest.mapper.."

        // Persistence packages
        private const val PERSISTENCE_PACKAGE = "$SECONDARY_ADAPTER_PACKAGE.persistence.."
        private const val PERSISTENCE_DOCUMENT_PACKAGE = "$SECONDARY_ADAPTER_PACKAGE.persistence.document.."
        private const val PERSISTENCE_MAPPER_PACKAGE = "$SECONDARY_ADAPTER_PACKAGE.persistence.mapper.."

        private const val CONFIG_PACKAGE = "..config.."
        private const val MAPPER = "..mapper.."
    }

    @Nested
    @DisplayName("REST Controller Rules")
    inner class RestControllerRules {
        @Test
        @DisplayName("should have controller classes end with Controller suffix")
        fun `should have controller classes end with Controller suffix`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .asSequence()
                .filter { it.resideInPackage(REST_PACKAGE) }
                .filter { !it.resideInPackage(REST_DTO_PACKAGE) }
                .filter { !it.resideInPackage(REST_MAPPER_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .toList()
                .assertTrue(testName = "Controller classes should end with 'Controller'") { clazz ->
                    clazz.hasNameEndingWith("Controller")
                }
        }

        @Test
        @DisplayName("should have controllers annotated with @RestController")
        fun `should have controllers annotated with RestController`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .asSequence()
                .filter { it.resideInPackage(REST_PACKAGE) }
                .filter { !it.resideInPackage(REST_DTO_PACKAGE) }
                .filter { !it.resideInPackage(REST_MAPPER_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .toList()
                .withNameEndingWith("Controller")
                .assertTrue(testName = "Controller classes should have @RestController annotation") { clazz ->
                    clazz.hasAnnotationWithName("RestController")
                }
        }

        @Test
        @DisplayName("should have controllers only use primary port use cases")
        fun `should have controllers only use primary port use cases`() {
            val portPrimary =
                Konsist
                    .scopeFromModule("application")
                    .interfaces()
                    .filter { it.resideInPackage(PRIMARY_PORT_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .map { it.name }

            val controllers =
                Konsist
                    .scopeFromModule("infrastructure")
                    .classes()
                    .asSequence()
                    .filter { it.resideInPackage(REST_PACKAGE) }
                    .filter { !it.resideInPackage(REST_DTO_PACKAGE) }
                    .filter { !it.resideInPackage(REST_MAPPER_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .toList()
                    .withNameEndingWith("Controller")

            val invalidControllers =
                controllers.filter { controller ->
                    val params = controller.primaryConstructor?.parameters ?: emptyList()
                    params.any { param ->
                        param.type.name !in portPrimary
                    }
                }

            assert(invalidControllers.isEmpty()) {
                val violations =
                    invalidControllers.joinToString("\n") { controller ->
                        val invalidParams =
                            controller.primaryConstructor
                                ?.parameters
                                .orEmpty()
                                .filter { param ->
                                    param.type.name !in portPrimary
                                }.map { "${it.name}: ${it.type.name}" }

                        "Class [${controller.name}] has invalid constructor parameters: $invalidParams"
                    }
                "Controller constructor parameters should be primary ports (interfaces), but violations:\n$violations"
            }
        }
    }

    @Nested
    @DisplayName("REST DTO Rules")
    inner class RestDtoRules {
        @Test
        @DisplayName("should have DTO classes follow naming pattern (Dto, Request, or Response)")
        fun `should have DTO classes follow naming pattern`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(REST_DTO_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "REST DTOs should end with Dto, RequestDto, or ResponseDto") { clazz ->
                    clazz.hasNameEndingWith("Dto") ||
                        clazz.hasNameEndingWith("RequestDto") ||
                        clazz.hasNameEndingWith("ResponseDto")
                }
        }

        @Test
        @DisplayName("should have DTOs be data classes or enum classes")
        fun `should have DTOs be data classes or enum classes`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(REST_DTO_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "REST DTOs should be data classes or enum classes") { clazz ->
                    clazz.hasModifier(KoModifier.DATA) || clazz.hasModifier(KoModifier.ENUM)
                }
        }

        @Test
        @DisplayName("should not have REST DTOs depend on domain entities directly")
        fun `should not have REST DTOs depend on domain entities`() {
            val domainEntities =
                Konsist
                    .scopeFromModule("domain")
                    .classes()
                    .filter { it.hasAnnotationWithName("DomainEntity") || it.hasAnnotationWithName("ValueObject") }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .map { it.name }

            val dtos =
                Konsist
                    .scopeFromModule("infrastructure")
                    .classes()
                    .filter { it.resideInPackage(REST_DTO_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .withNameEndingWith("Dto")

            val invalidDtos =
                dtos.filter { dto ->
                    val typedParams = dto.primaryConstructor?.parameters.orEmpty()
                    typedParams.any { it.type.name in domainEntities }
                }

            assert(invalidDtos.isEmpty()) {
                val violations =
                    invalidDtos.joinToString("\n") { dto ->
                        val invalidParams =
                            dto.primaryConstructor
                                ?.parameters
                                .orEmpty()
                                .filter { param ->
                                    param.type.name in domainEntities
                                }.map { "${it.name}: ${it.type.name}" }

                        "Class [${dto.name}] has invalid parameters: $invalidParams"
                    }
                "Dto parameters should not depend on Domain, but violations:\n$violations"
            }
        }
    }

    @Nested
    @DisplayName("REST Mapper Rules")
    inner class RestMapperRules {
        @Test
        @DisplayName("should have mapper files end with Mapper suffix")
        fun `should have mapper files end with Mapper suffix`() {
            Konsist
                .scopeFromModule("infrastructure")
                .files
                .filter { it.resideInPath(MAPPER) }
                .assertTrue(testName = "REST mapper files should end with 'Mapper'") { file ->
                    file.hasNameEndingWith("Mapper")
                }
        }
    }

    @Nested
    @DisplayName("Configuration Rules")
    inner class ConfigurationRules {
        @Test
        @DisplayName("should have configuration classes annotated with @Configuration")
        fun `should have configuration classes annotated with Configuration`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(CONFIG_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .withNameEndingWith("Config")
                .assertTrue(testName = "Config classes should have @Configuration annotation") { clazz ->
                    clazz.hasAnnotationWithName("Configuration")
                }
        }

        @Test
        @DisplayName("should have configuration classes end with Config suffix")
        fun `should have configuration classes end with Config suffix`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(CONFIG_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .filter { it.hasAnnotationWithName("Configuration") }
                .assertTrue(testName = "@Configuration classes should end with 'Config'") { clazz ->
                    clazz.hasNameEndingWith("Config")
                }
        }

        @Test
        @DisplayName("should have configuration classes define @Bean methods")
        fun `should have configuration classes with Bean methods`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .asSequence()
                .filter { it.resideInPackage(CONFIG_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .filter { it.hasAnnotationWithName("Configuration") }
                .flatMap {
                    it.functions(
                        includeNested = false,
                        includeLocal = false,
                    )
                }
                // All public methods should have '@Bean' annotation
                .filterNot { it.hasPrivateModifier }
                .toList()
                .assertTrue(testName = "Configuration classes should have @Bean methods") { func ->
                    func.hasAnnotationWithName("Bean")
                }
        }
    }

    @Nested
    @DisplayName("Secondary Adapter Rules")
    inner class SecondaryAdapterRules {
        @Test
        @DisplayName("should have repository implementations implement secondary adapter interfaces")
        fun `should have repositories implement secondary port interfaces`() {
            val repositories =
                Konsist
                    .scopeFromModule("application")
                    .interfaces()
                    .filter { it.resideInPackage(SECONDARY_PORT_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .withNameEndingWith("Repository")
                    .map { it.name }

            val persistenceRepositories =
                Konsist
                    .scopeFromModule("infrastructure")
                    .classes()
                    .filter { it.resideInPackage(SECONDARY_ADAPTER_PACKAGE) }
                    .filter { !it.hasNameEndingWith("Companion") }
                    .withNameEndingWith("Repository")

            val invalidPersistenceRepositories =
                persistenceRepositories.filter { repo ->
                    val repoParents = repo.parentInterfaces()
                    repoParents.any { it.name !in repositories }
                }

            assert(invalidPersistenceRepositories.isEmpty()) {
                val violations =
                    invalidPersistenceRepositories.joinToString("\n") { repo ->
                        val invalidParents =
                            repo
                                .parentInterfaces()
                                .filter { parent ->
                                    parent.name in repositories
                                }.map { it.name }

                        "Class [${repo.name}] has invalid parents: $invalidParents"
                    }
                "Repositories should depend only on secondary ports, but violations:\n$violations"
            }
        }

        @Test
        @DisplayName("should have repository classes end with Repository suffix")
        fun `should have repository classes end with Repository suffix`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .asSequence()
                .filter { it.resideInPackage(PERSISTENCE_PACKAGE) }
                .filter { !it.resideInPackage(PERSISTENCE_DOCUMENT_PACKAGE) }
                .filter { !it.resideInPackage(PERSISTENCE_MAPPER_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .filter { it.hasAnnotationWithName("Repository") || it.hasAnnotationWithName("Component") }
                .toList()
                .assertTrue(testName = "Repository classes should end with 'Repository'") { clazz ->
                    clazz.hasNameEndingWith("Repository")
                }
        }
    }

    @Nested
    @DisplayName("Persistence Document Rules")
    inner class PersistenceDocumentRules {
        @Test
        @DisplayName("should have document classes end with Document or Entity suffix")
        fun `should have document classes end with Document or Entity suffix`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(PERSISTENCE_DOCUMENT_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Document classes should end with 'Document' or 'Entity'") { clazz ->
                    clazz.hasNameEndingWith("Document") || clazz.hasNameEndingWith("Entity")
                }
        }

        @Test
        @DisplayName("should have documents annotated with @Document")
        fun `should have documents annotated with Document annotation`() {
            Konsist
                .scopeFromModule("infrastructure")
                .classes()
                .filter { it.resideInPackage(PERSISTENCE_DOCUMENT_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Document classes should have @Document annotation") { clazz ->
                    clazz.hasAnnotationWithName("Document")
                }
        }
    }

    @Nested
    @DisplayName("General Architecture Rules")
    inner class GeneralArchitectureRules {
        @Test
        @DisplayName("should not have primary adapters depend on secondary adapters")
        fun `should not have primary adapters depend on secondary adapters`() {
            val module =
                Konsist
                    .scopeFromModule("infrastructure")

            val primaryAdapterHasClasses =
                module
                    .classes()
                    .filter { it.resideInPackage(PRIMARY_ADAPTER_PACKAGE) }
                    .size > 1

            val secondaryAdapterHasClasses =
                module
                    .classes()
                    .filter { it.resideInPackage(SECONDARY_ADAPTER_PACKAGE) }
                    .size > 1

            if (primaryAdapterHasClasses && secondaryAdapterHasClasses) {
                module.assertArchitecture {
                    val primaryAdapter = Layer("PrimaryAdapter", "$PRIMARY_ADAPTER_PACKAGE..")
                    val secondaryAdapter = Layer("SecondaryAdapter", "$SECONDARY_ADAPTER_PACKAGE..")
                    primaryAdapter.doesNotDependOn(secondaryAdapter)
                }
            }
        }
    }
}
