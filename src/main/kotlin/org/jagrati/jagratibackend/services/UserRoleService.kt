package org.jagrati.jagratibackend.services

import AssignRoleToUserRequest
import UserDetailsWithRolesAndPermissions
import PermissionListResponse
import PermissionResponse
import RemoveRoleFromUserRequest
import RoleSummaryResponse
import UserRoleAssignmentResponse
import UserSummaryDTO
import UserWithRolesListResponse
import UserWithRolesResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.map

@Service
class UserRoleService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository,
    private val permissionRoleRepository: RolePermissionRepository
) {
    @Transactional
    fun assignRoleToUser(request: AssignRoleToUserRequest, assignedByPid: String): UserRoleAssignmentResponse {
        val user = userRepository.findUserByPid(request.userPid) ?: throw IllegalArgumentException("User not found")
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val assignedBy =
            userRepository.findUserByPid(assignedByPid) ?: throw IllegalArgumentException("Assigning user not found")
        val userRole = UserRole(user = user, role = role, assignedBy = assignedBy)
        userRoleRepository.save(userRole)
        return UserRoleAssignmentResponse(user.pid, role.id, "Role assigned to user")
    }

    @Transactional
    fun removeRoleFromUser(request: RemoveRoleFromUserRequest, removedByPid: String): UserRoleAssignmentResponse {
        val user = userRepository.findUserByPid(request.userPid) ?: throw IllegalArgumentException("User not found")
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val userRole = userRoleRepository.findByUserAndRole(user, role)
            ?: throw IllegalArgumentException("User does not have this role")
        userRoleRepository.delete(userRole)
        return UserRoleAssignmentResponse(user.pid, role.id, "Role removed from user")
    }

    fun getAllUsersWithRoles(): UserWithRolesListResponse {
        val users = userRepository.findAll()
        val result = users.map { user ->
            val roles = userRoleRepository.findByUser(user).map { ur ->
                RoleSummaryResponse(
                    id = ur.role.id,
                    name = ur.role.name,
                    description = ur.role.description,
                    isActive = ur.role.isActive,
                )
            }
            UserWithRolesResponse(
                pid = user.pid,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                roles = roles
            )
        }
        return UserWithRolesListResponse(result)
    }

    fun fetchDetailsOfUser(user: User): UserDetailsWithRolesAndPermissions {
        val roles = userRoleRepository.findByUser(user)
        val userPermissions = mutableListOf<PermissionResponse>()
        roles.forEach { userRole ->
            permissionRoleRepository.findByRole(userRole.role).forEach { permissionRole ->
                userPermissions.add(
                    PermissionResponse(
                        id = permissionRole.permission.id,
                        name = permissionRole.permission.name,
                        description = permissionRole.permission.description,
                        module = permissionRole.permission.module.name,
                        action = permissionRole.permission.action.name
                    )
                )
            }
        }
        return UserDetailsWithRolesAndPermissions(
            permissions = PermissionListResponse(userPermissions),
            userDetails = UserSummaryDTO(
                pid = user.pid,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                profileImageUrl = user.profilePictureUrl
            ),
            roles = roles.map { ur ->
                RoleSummaryResponse(
                    id = ur.role.id,
                    name = ur.role.name,
                    description = ur.role.description,
                    isActive = ur.role.isActive,
                )
            }
        )
    }
}