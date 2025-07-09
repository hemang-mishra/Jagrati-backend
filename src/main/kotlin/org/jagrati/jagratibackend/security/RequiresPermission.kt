package org.jagrati.jagratibackend.security

import org.jagrati.jagratibackend.entities.enums.AllPermissions

/**
 * Custom annotation to specify required permissions for methods.
 * This works with PermissionAspect to enforce permission checks.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(
    val value: AllPermissions
)
