import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

// --- Role Management ---
data class RoleResponse(val id: Long, val name: String, val description: String?, val isActive: Boolean)
data class RoleListResponse(val roles: List<RoleResponse>)

// --- Permission Management ---
data class PermissionResponse(val id: Long, val name: String, val description: String?, val module: String, val action: String)
data class PermissionListResponse(val permissions: List<PermissionResponse>)
data class PermissionRoleAssignmentResponse(val permissionId: Long, val roleId: Long, val message: String)

// --- User Management ---
data class UserRoleAssignmentResponse(val userPid: String, val roleId: Long, val message: String)

// --- Volunteer Request Management ---
data class VolunteerRequestActionResponse(val requestId: Long, val status: String, val message: String)

data class DetailedVolunteerRequestResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val rollNumber: String?,
    val alternateEmail: String?,
    val batch: String?,
    val programme: String?,
    val dateOfBirth: LocalDate,
    val contactNumber: String?,
    val profileImageUrl: String?,
    val college: String?,
    val branch: String?,
    val yearOfStudy: Int?,
    val address: AddressDTO?,
    val status: String,
    val createdAt: LocalDateTime,
    val requestedByUser: UserSummaryDTO,
    val reviewedByUser: UserSummaryDTO?,
    val reviewedAt: LocalDateTime?
)

data class AddressDTO(
    val streetAddress1: String?,
    val streetAddress2: String?,
    val pincode: String?,
    val city: String?,
    val state: String?
)

data class UserSummaryDTO(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String?
)

data class DetailedVolunteerRequestListResponse(
    val requests: List<DetailedVolunteerRequestResponse>
)


// --- Bulk Permissions with Roles ---
data class PermissionWithRolesResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val module: String,
    val action: String,
    val assignedRoles: List<RoleSummaryResponse>
)
data class PermissionWithRolesListResponse(
    val permissions: List<PermissionWithRolesResponse>
)

// --- Bulk Users with Roles ---
data class UserWithRolesResponse(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roles: List<RoleSummaryResponse>
)
data class UserWithRolesListResponse(
    val users: List<UserWithRolesResponse>
)

// --- My Volunteer Requests ---
data class MyVolunteerRequestResponse(
    val id: Long,
    val status: String,
    val createdAt: java.time.LocalDateTime,
    val reviewedAt: java.time.LocalDateTime?,
    val message: String?
    // Add other relevant fields as needed
)
data class MyVolunteerRequestListResponse(
    val requests: List<MyVolunteerRequestResponse>
)

// --- Role Summary DTO ---
data class RoleSummaryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean
)


// --- Role Management ---
data class CreateRoleRequest(val name: String, val description: String?)
data class UpdateRoleRequest(val id: Long, val name: String, val description: String?)
data class DeactivateRoleRequest(val id: Long)

// --- Permission Management ---
data class AssignRoleToPermissionRequest(val permissionId: Long, val roleId: Long)
data class RemoveRoleFromPermissionRequest(val permissionId: Long, val roleId: Long)

// --- User Management ---
data class AssignRoleToUserRequest(val userPid: String, val roleId: Long)
data class RemoveRoleFromUserRequest(val userPid: String, val roleId: Long)

// --- Volunteer Request Management ---
data class ApproveVolunteerRequest(val requestId: Long)
data class RejectVolunteerRequest(val requestId: Long, val reason: String?)
data class CreateVolunteerRequest(
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    @field:NotBlank val gender: String,
    val rollNumber: String?,
    val alternateEmail: String?,
    val batch: String?,
    val programme: String?,
    val streetAddress1: String?,
    val streetAddress2: String?,
    val pincode: String?,
    val city: String?,
    val state: String?,
    @field:NotBlank val dateOfBirth: String, // Will be parsed to LocalDate
    val contactNumber: String?,
    val profileImageUrl: String?,
    val college: String?,
    val branch: String?,
    val yearOfStudy: Int?
)


data class UserDetailsWithRolesAndPermissions(
    val userDetails: UserSummaryDTO,
    val roles: List<RoleSummaryResponse>,
    val permissions: PermissionListResponse
)