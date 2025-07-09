package org.jagrati.jagratibackend.services

import CreateRoleRequest
import DeactivateRoleRequest
import RoleListResponse
import RoleResponse
import UpdateRoleRequest
import org.jagrati.jagratibackend.entities.Role
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository
) {
    fun createRole(request: CreateRoleRequest): RoleResponse {
        val role = Role(
            name = request.name,
            description = request.description,
            isActive = true
        )
        val saved = roleRepository.save(role)
        return RoleResponse(saved.id, saved.name, saved.description, saved.isActive)
    }

    fun updateRole(request: UpdateRoleRequest): RoleResponse {
        val role = roleRepository.findById(request.id).orElseThrow { IllegalArgumentException("Role not found") }
        val updated = role.copy(
            name = request.name,
            description = request.description
        )
        val saved = roleRepository.save(updated)
        return RoleResponse(saved.id, saved.name, saved.description, saved.isActive)
    }

    fun deactivateRole(request: DeactivateRoleRequest): RoleResponse {
        val role = roleRepository.findById(request.id).orElseThrow { IllegalArgumentException("Role not found") }
        val updated = role.copy(isActive = false)
        val saved = roleRepository.save(updated)
        return RoleResponse(saved.id, saved.name, saved.description, saved.isActive)
    }

    fun getAllRoles(): RoleListResponse {
        val roles = roleRepository.findAll().map { RoleResponse(it.id, it.name, it.description, it.isActive) }
        return RoleListResponse(roles)
    }

    fun getRoleById(id: Long): RoleResponse {
        val role = roleRepository.findById(id).orElseThrow { IllegalArgumentException("Role not found") }
        return RoleResponse(role.id, role.name, role.description, role.isActive)
    }
}