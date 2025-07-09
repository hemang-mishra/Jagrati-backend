package org.jagrati.jagratibackend.config

import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.jagrati.jagratibackend.security.CustomMethodSecurityExpressionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig {
    @Bean
    fun methodSecurityExpressionHandler(
        rolePermissionRepo: RolePermissionRepository,
    ): MethodSecurityExpressionHandler{
        return CustomMethodSecurityExpressionHandler(rolePermissionRepo)
    }
}