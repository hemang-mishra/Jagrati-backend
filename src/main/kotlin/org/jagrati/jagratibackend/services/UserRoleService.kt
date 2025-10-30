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
import org.jagrati.jagratibackend.dto.toDTO
import org.jagrati.jagratibackend.dto.toResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.repository.GroupRepository
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.StudentRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.repository.VillageRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.collections.map
import kotlin.jvm.optionals.getOrNull

@Service
class UserRoleService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository,
    private val permissionRoleRepository: RolePermissionRepository,
    private val villageRepository: VillageRepository,
    private val groupRepository: GroupRepository,
    private val studentRepository: StudentRepository,
    private val volunteerRepository: VolunteerRepository,
    private val fcmService: FCMService
) {
    @Transactional
    fun assignRoleToUser(request: AssignRoleToUserRequest, assignedByPid: String): UserRoleAssignmentResponse {
        val user = userRepository.findUserByPid(request.userPid) ?: throw IllegalArgumentException("User not found")
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val assignedBy =
            userRepository.findUserByPid(assignedByPid) ?: throw IllegalArgumentException("Assigning user not found")
        val userRole = UserRole(user = user, role = role, assignedBy = assignedBy)
        userRoleRepository.save(userRole)
        //Notify user to sync permissions
        fcmService.sendSyncNotificationToUsers(listOf(user))
        return UserRoleAssignmentResponse(user.pid, role.id, "Role assigned to user")
    }

    @Transactional
    fun removeRoleFromUser(request: RemoveRoleFromUserRequest, removedByPid: String): UserRoleAssignmentResponse {
        val user = userRepository.findUserByPid(request.userPid) ?: throw IllegalArgumentException("User not found")
        val role = roleRepository.findById(request.roleId).orElseThrow { IllegalArgumentException("Role not found") }
        val userRole = userRoleRepository.findByUserAndRole(user, role)
            ?: throw IllegalArgumentException("User does not have this role")
        //Notify user to sync permissions
        fcmService.sendSyncNotificationToUsers(listOf(user))
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

    fun fetchDetailsOfUser(user: User, timeMillis: Long): UserDetailsWithRolesAndPermissions {
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
        var dto = UserDetailsWithRolesAndPermissions(
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
        if(volunteerRepository.existsById(user.pid)){
            val updatedAfter = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault())
            val volunteer = volunteerRepository.findById(user.pid).getOrNull()
            val volunteers = volunteerRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toResponse() }
            val students = studentRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toResponse() }
            val villages = villageRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toDTO() }
            val groups = groupRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toDTO() }
            dto = dto.copy(
                volunteers = volunteers,
                students = students,
                volunteerProfile = volunteer?.toResponse(),
                villages = villages,
                groups = groups,
                isVolunteer = true
            )
        }
        return dto
    }
}