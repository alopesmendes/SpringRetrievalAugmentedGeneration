package com.ailtontech.annotation

/**
 * Marks a class as a Domain Entity.
 *
 * Domain entities are objects with a distinct identity that runs through time
 * and different representations. They contain business logic and are the core
 * of the domain model.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DomainEntity
