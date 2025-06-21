package org.jagrati.jagratibackend.security

import org.aopalliance.intercept.MethodInvocation
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication

class CustomMethodSecurityExpressionHandler(
    val rolePermissionRepo: RolePermissionRepository,
) : DefaultMethodSecurityExpressionHandler() {

    override fun createSecurityExpressionRoot(
        authentication: Authentication,
        invocation: MethodInvocation
    ): MethodSecurityExpressionOperations {
        val root = CustomMethodSecurityExpressionRoot(authentication, rolePermissionRepo)
        root.setPermissionEvaluator(permissionEvaluator)
        root.setTrustResolver(trustResolver)
        root.setRoleHierarchy(roleHierarchy)
        root.setDefaultRolePrefix(defaultRolePrefix)
        return root
    }
}