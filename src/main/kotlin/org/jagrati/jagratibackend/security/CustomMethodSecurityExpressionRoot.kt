package org.jagrati.jagratibackend.security

import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.util.logging.Logger

class CustomMethodSecurityExpressionRoot(
    private val authentication: Authentication,
    private val rolePermissionRepo: RolePermissionRepository
): SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    private var filterObject: Any? = null
    private var returnObject: Any? = null
    private var target: Any? = null
    private val logger = Logger.getLogger(CustomMethodSecurityExpressionRoot::class.java.name)

    fun hasPermission(permissionName: String): Boolean {
        if(!authentication.isAuthenticated) return false
        logger.info("Checking permission $permissionName")
        authentication.authorities.forEach {
            logger.info("Authority $it")
        }
        val roles = authentication.authorities
            .map { (it as GrantedAuthority).authority}

        return rolePermissionRepo.existsByRoleNameInAndPermissionName(roles, permissionName)
    }

    override fun setFilterObject(filterObject: Any?) {
        this.filterObject = filterObject
    }

    override fun getFilterObject(): Any? = filterObject

    override fun setReturnObject(returnObject: Any?) {
        this.returnObject = returnObject
    }

    override fun getReturnObject(): Any? = returnObject

    override fun getThis(): Any? = target

    fun setThis(target: Any?) {
        this.target = target
    }

}