package com.ailtontech.annotation

/**
 * Marks a class as a Domain Service.
 *
 * Domain services contain business logic that doesn't naturally fit within
 * a single entity or value object. They operate on domain objects and
 * encapsulate domain operations that span multiple entities.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DomainService
