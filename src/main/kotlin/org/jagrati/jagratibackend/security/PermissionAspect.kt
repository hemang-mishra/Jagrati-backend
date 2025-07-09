package org.jagrati.jagratibackend.security

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class PermissionAspect(
    private val rolePermissionRepository: RolePermissionRepository
) {
    @Around("@annotation(org.jagrati.jagratibackend.security.RequiresPermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val annotation = method.getAnnotation(RequiresPermission::class.java)

        val authentication = SecurityContextHolder.getContext().authentication
        val expressionRoot = CustomMethodSecurityExpressionRoot(
            authentication, rolePermissionRepository
        )

        if(expressionRoot.hasPermission(annotation.value.name)){
            return joinPoint.proceed()
        } else {
            throw AccessDeniedException("Access denied: Required Permission: ${annotation.value.name}")
        }
    }
}
