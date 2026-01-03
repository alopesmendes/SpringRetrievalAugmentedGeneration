package com.ailtontech.architecture

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Architecture tests for the Domain module using Konsist.
 *
 * The Domain module is the core of the hexagonal architecture and must:
 * - Have ZERO external dependencies (no Spring, no infrastructure frameworks)
 * - Only depend on Kotlin stdlib
 * - Contain pure business logic
 */
@DisplayName("Domain Module Architecture Tests")
class DomainArchitectureTest {
    companion object {
        private const val ENTITY_PACKAGE = "..model.entity.."
        private const val VALUE_OBJECT_PACKAGE = "..model.valueobject.."
        private const val SERVICE_PACKAGE = "..service.."
        private const val ANNOTATION_PACKAGE = "..annotation.."
        private const val EXCEPTION_PACKAGE = "..exception.."

        // Forbidden package prefixes
        private val FORBIDDEN_IMPORTS =
            listOf(
                "org.springframework",
                "jakarta.",
                "javax.",
                "com.mongodb",
                "org.hibernate",
                "..application..",
                "..infrastructure..",
            )
    }

    @Nested
    @DisplayName("Module Independence")
    inner class ModuleIndependence {
        @Test
        @DisplayName("should have zero forbidden imports")
        fun `should have zero forbidden imports`() {
            Konsist
                .scopeFromModule("domain")
                .files
                .assertFalse(testName = "Domain should not have forbidden imports") { file ->
                    file.imports.any { import ->
                        FORBIDDEN_IMPORTS.any { forbidden ->
                            import.name.startsWith(forbidden)
                        }
                    }
                }
        }

        @Test
        @DisplayName("should only import from allowed packages")
        fun `should only import from allowed packages`() {
            val baseAllowed =
                listOf(
                    "java.",
                    "javax.annotation",
                    "kotlin",
                    "kotlinx",
                    "org.jetbrains.annotations",
                    "com.ailtontech.annotation.",
                )

            val scope =
                Konsist
                    .scopeFromModule("domain")
                    .files
                    .filterNot { it.resideInSourceSet("test") }

            scope.assertTrue { file ->
                val isEntity = file.resideInPath("..entity..")
                val isValueObject = file.resideInPath("..valueobjects..")
                val isException = file.resideInPath("..exception..")

                file.imports.all { import ->
                    val isBaseAllowed = baseAllowed.any { prefix -> import.name.contains(prefix) }

                    when {
                        // Entities and Exception can import base stuff OR any valueobject package
                        isEntity || isException -> isBaseAllowed || import.name.contains(".valueobjects.")

                        // Value Objects can ONLY import base stuff (which includes .annotation.)
                        isValueObject -> isBaseAllowed

                        // Other domain files (like Services/Interfaces)
                        else -> isBaseAllowed || import.name.contains(".entity.") || import.name.contains(".exception.")
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Entity Package Rules")
    inner class EntityPackageRules {
        @Test
        @DisplayName("should have all entity classes annotated with @DomainEntity")
        fun `should have all entity classes annotated with DomainEntity`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(ENTITY_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Entity classes should be annotated with @DomainEntity") { clazz ->
                    clazz.hasAnnotationWithName("DomainEntity")
                }
        }

        @Test
        @DisplayName("should have entity classes be top level classes")
        fun `should have entity classes be top level classes`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(ENTITY_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Entity classes should be top-level") { clazz ->
                    clazz.isTopLevel
                }
        }
    }

    @Nested
    @DisplayName("Value Object Package Rules")
    inner class ValueObjectPackageRules {
        @Test
        @DisplayName("should have all value object classes annotated with @ValueObject")
        fun `should have all value object classes annotated with ValueObject`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(VALUE_OBJECT_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Value object classes should be annotated with @ValueObject") { clazz ->
                    clazz.hasAnnotationWithName("ValueObject")
                }
        }

        @Test
        @DisplayName("should have value objects be value classes (annotated with @JvmInline)")
        fun `should have value objects be value classes`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(VALUE_OBJECT_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Value object classes should be value classes with @JvmInline") { clazz ->
                    clazz.hasAnnotationWithName("JvmInline") || clazz.hasModifier(KoModifier.VALUE)
                }
        }
    }

    @Nested
    @DisplayName("Service Package Rules")
    inner class ServicePackageRules {
        @Test
        @DisplayName("should have all service classes annotated with @DomainService")
        fun `should have all service classes annotated with DomainService`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(SERVICE_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Service classes should be annotated with @DomainService") { clazz ->
                    clazz.hasAnnotationWithName("DomainService")
                }
        }

        @Test
        @DisplayName("should have service classes end with Service suffix")
        fun `should have service classes end with Service suffix`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(SERVICE_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Service classes should end with 'Service'") { clazz ->
                    clazz.hasNameEndingWith("Service")
                }
        }
    }

    @Nested
    @DisplayName("Annotation Package Rules")
    inner class AnnotationPackageRules {
        @Test
        @DisplayName("should have only annotation classes in annotation package")
        fun `should have only annotation classes in annotation package`() {
            Konsist
                .scopeFromModule("domain")
                .classesAndInterfaces()
                .filter { it.resideInPackage(ANNOTATION_PACKAGE) }
                .assertTrue(testName = "Annotation package should only contain annotation classes") { clazz ->
                    clazz.hasModifier(KoModifier.ANNOTATION)
                }
        }
    }

    @Nested
    @DisplayName("Exception Package Rules")
    inner class ExceptionPackageRules {
        @Test
        @DisplayName("should have exception classes end with Exception suffix")
        fun `should have exception classes end with Exception suffix`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(EXCEPTION_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .assertTrue(testName = "Exception classes should end with 'Exception'") { clazz ->
                    clazz.hasNameEndingWith("Exception")
                }
        }

        @Test
        @DisplayName("should have exception classes extend RuntimeException or Exception")
        fun `should have exception classes extend RuntimeException`() {
            Konsist
                .scopeFromModule("domain")
                .classes()
                .filter { it.resideInPackage(EXCEPTION_PACKAGE) }
                .filter { !it.hasNameEndingWith("Companion") }
                .withNameEndingWith("Exception")
                .assertTrue(testName = "Exception classes should extend RuntimeException or Exception") { clazz ->
                    clazz.hasParentWithName("RuntimeException") ||
                        clazz.hasParentWithName("Exception") ||
                        clazz.hasParentWithName("DomainException") ||
                        clazz.parents().any { parent ->
                            parent.name.endsWith("Exception")
                        }
                }
        }
    }

    @Nested
    @DisplayName("General Architecture Rules")
    inner class GeneralArchitectureRules {
        @Test
        @DisplayName("should have interfaces follow naming convention starting with I")
        fun `should have interfaces follow naming convention`() {
            Konsist
                .scopeFromModule("domain")
                .interfaces()
                .assertTrue(testName = "Interfaces should start with 'I'") { iface ->
                    iface.hasNameStartingWith("I")
                }
        }

        @Test
        @DisplayName("should have all classes, interfaces and objects with Kdoc")
        fun `should have all classes, interfaces and objects with Kdoc`() {
            Konsist
                .scopeFromModule("domain")
                .classesAndInterfacesAndObjects()
                .filterNot { it.resideInSourceSet("test") }
                .filter { !it.hasNameEndingWith("Companion") }
                // With the exception of exception classes
                .filterNot { it.resideInPackage(EXCEPTION_PACKAGE) }
                .assertTrue(testName = "Classes, interfaces and objects should have Kdoc comments") { clazz ->
                    clazz.hasKDoc
                }
        }
    }
}
