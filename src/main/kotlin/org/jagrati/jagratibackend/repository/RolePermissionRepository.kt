package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Permission
import org.jagrati.jagratibackend.entities.Role
import org.jagrati.jagratibackend.entities.RolePermission
import org.springframework.data.jpa.repository.JpaRepository

interface RolePermissionRepository: JpaRepository<RolePermission, Long> {
    fun existsByRoleNameInAndPermissionName(roles: List<String>, permission: String): Boolean
    fun findByRoleAndPermission(role: Role, permission: Permission): RolePermission?
}