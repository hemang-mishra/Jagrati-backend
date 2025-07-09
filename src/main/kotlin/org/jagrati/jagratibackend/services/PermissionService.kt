package org.jagrati.jagratibackend.services

import AssignRoleToPermissionRequest
import PermissionListResponse
import PermissionResponse
import PermissionRoleAssignmentResponse
import PermissionWithRolesListResponse
import PermissionWithRolesResponse
import RemoveRoleFromPermissionRequest
import RoleSummaryResponse
import org.jagrati.jagratibackend.entities.RolePermission
import org.jagrati.jagratibackend.repository.PermissionRepository
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val roleRepository: RoleRepository
) {
    fun getAllPermissions(): PermissionListResponse {
        val permissions = permissionRepository.findAll().map {
            PermissionResponse(it.id, it.name, it.description, it.module.name, it.action.name)
        }
        return PermissionListResponse(permissions)
    }

    fun getPermissionById(id: Long): PermissionResponse {
        val permission = permissionRepository.findById(id).orElseThrow { IllegalArgumentException("Permission not found") }
        return PermissionResponse(
            permission.id,
            permission.name,
            permission.description,
            permission.module.name,
            permission.action.name
        )
    }

    fun assignRoleToPermission(request: AssignRoleToPermissionRequest): PermissionRoleAssignmentResponse {
        val permission = permissionRepository.findById(request.permissionId).orElseThrow { IllegalArgumentException("Permission not found") }
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val existing = rolePermissionRepository.findByRoleAndPermission(role, permission)
        return if (existing != null) {
            PermissionRoleAssignmentResponse(permission.id, role.id, "Role already assigned to permission")
        } else {
            val rolePermission = RolePermission(role = role, permission = permission)
            rolePermissionRepository.save(rolePermission)
            PermissionRoleAssignmentResponse(permission.id, role.id, "Role assigned to permission")
        }
    }

    fun removeRoleFromPermission(request: RemoveRoleFromPermissionRequest): PermissionRoleAssignmentResponse {
        val permission = permissionRepository.findById(request.permissionId).orElseThrow { IllegalArgumentException("Permission not found") }
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val existing = rolePermissionRepository.findByRoleAndPermission(role, permission)
        return if (existing == null) {
            PermissionRoleAssignmentResponse(permission.id, role.id, "Role was not assigned to permission")
        } else {
            rolePermissionRepository.delete(existing)
            PermissionRoleAssignmentResponse(permission.id, role.id, "Role removed from permission")
        }
    }

    fun getAllPermissionsWithRoles(): PermissionWithRolesListResponse {
        val permissions = permissionRepository.findAll()
        val result = permissions.map { perm ->
            val assignedRoles = rolePermissionRepository.findByPermission(perm).map { rp ->
                RoleSummaryResponse(
                    id = rp.role.id,
                    name = rp.role.name,
                    description = rp.role.description,
                    isActive = rp.role.isActive,
                )
            }
            PermissionWithRolesResponse(
                id = perm.id,
                name = perm.name,
                description = perm.description,
                module = perm.module.name,
                action = perm.action.name,
                assignedRoles = assignedRoles
            )
        }
        return PermissionWithRolesListResponse(result)
    }

    fun getRolesForPermission(permissionId: Long): List<RoleSummaryResponse> {
        val permission = permissionRepository.findById(permissionId).orElseThrow { IllegalArgumentException("Permission not found") }
        val rolePermissions = rolePermissionRepository.findByPermission(permission)
        return rolePermissions.map { rp ->
            RoleSummaryResponse(
                id = rp.role.id,
                name = rp.role.name,
                description = rp.role.description,
                isActive = rp.role.isActive
            )
        }
    }
}