package com.ailtontech.annotation

/**
 * Marks a class as a Value Object.
 *
 * Value objects are immutable objects that are defined by their attributes
 * rather than an identity. They should be compared by value equality and
 * contain validation logic in their constructors.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ValueObject
