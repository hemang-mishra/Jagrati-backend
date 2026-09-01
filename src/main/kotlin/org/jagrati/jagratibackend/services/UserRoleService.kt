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
import org.jagrati.jagratibackend.dto.PersonMasking
import org.jagrati.jagratibackend.dto.toDTO
import org.jagrati.jagratibackend.dto.toResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
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
        val users = userRepository.findAllByDeletedAtIsNull()
        val result = users.filter { user -> user.isActive }.map { user ->
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
        // Holding a volunteer record answers "who is this person", not "may they act as a
        // volunteer". A PROVISIONAL record is someone whose roll number was typed at the
        // attendance screen and who has since signed in; the account is linked so they can
        // see their own history, but they have not applied or been approved yet. Reporting
        // them as a volunteer sends them to the dashboard and hides the application form.
        val volunteer = volunteerRepository.findByUserPidAndDeletedAtIsNull(user.pid)
        val isActiveVolunteer = volunteer?.status == VolunteerStatus.ACTIVE

        if (volunteer != null) {
            dto = dto.copy(
                volunteerProfile = volunteer.toResponse(),
                isVolunteer = isActiveVolunteer
            )
        }

        if (isActiveVolunteer) {
            val updatedAfter = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault())
            // Deleted rows are included on purpose: isActive=false is the tombstone the
            // client uses to drop its local copy. Masked so a deletion never ships names.
            val volunteers = volunteerRepository.findAllByUpdatedAtAfter(updatedAfter)
                .map { with(PersonMasking) { it.toMaskedResponse() } }
            val students = studentRepository.findAllByUpdatedAtAfter(updatedAfter)
                .map { with(PersonMasking) { it.toMaskedResponse() } }
            val villages = villageRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toDTO() }
            val groups = groupRepository.findAllByUpdatedAtAfter(updatedAfter).map { it.toDTO() }
            dto = dto.copy(
                volunteers = volunteers,
                students = students,
                villages = villages,
                groups = groups
            )
        }
        return dto
    }
}