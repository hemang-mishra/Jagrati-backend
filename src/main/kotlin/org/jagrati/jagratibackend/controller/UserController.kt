package org.jagrati.jagratibackend.controller

import AssignRoleToUserRequest
import UserDetailsWithRolesAndPermissions
import RemoveRoleFromUserRequest
import UserRoleAssignmentResponse
import UserWithRolesListResponse
import UserWithRolesResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.DeleteMyAccountRequest
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.enums.DeletionSource
import org.jagrati.jagratibackend.services.AuthService
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.services.UserRoleService
import org.jagrati.jagratibackend.services.UserService
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management API", description = "Endpoints for managing users and their roles")
class UserController(
    private val userRoleService: UserRoleService,
    private val userService: UserService,
    private val authService: AuthService,
) {
    @Operation(summary = "List all users", description = "Fetches all users. Optionally filter by name.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of users",
                content = [Content(schema = Schema(implementation = UserWithRolesListResponse::class))]
            )
        ]
    )
    @GetMapping
    @RequiresPermission(AllPermissions.USER_VIEW)
    fun getAllUsers(@RequestParam(required = false) search: String?): ResponseEntity<UserWithRolesListResponse> {
        // TODO: Implement search logic if needed
        return ResponseEntity.ok(userRoleService.getAllUsersWithRoles())
    }

    @Operation(summary = "Get user details", description = "Fetches details of a specific user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User details",
                content = [Content(schema = Schema(implementation = UserWithRolesResponse::class))]
            )
        ]
    )
    @GetMapping("/{pid}")
    @RequiresPermission(AllPermissions.USER_VIEW)
    fun getUserByPid(@PathVariable pid: String): ResponseEntity<UserWithRolesResponse> {
        val users = userRoleService.getAllUsersWithRoles().users
        val user = users.find { it.pid == pid } ?: throw IllegalArgumentException("User not found")
        return ResponseEntity.ok(user)
    }

    @Operation(summary = "Assign a role to a user", description = "Assigns a role to a user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role assigned",
                content = [Content(schema = Schema(implementation = UserRoleAssignmentResponse::class))]
            )
        ]
    )
    @PostMapping("/{pid}/roles")
    @RequiresPermission(AllPermissions.USER_ROLE_ASSIGN)
    fun assignRoleToUser(
        @PathVariable pid: String,
        @RequestBody request: AssignRoleToUserRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserRoleAssignmentResponse> {
        val assignedByPid = SecurityUtils.getCurrentUser()?.pid ?: throw IllegalArgumentException("User not found")
        return ResponseEntity.ok(userRoleService.assignRoleToUser(request.copy(userPid = pid), assignedByPid))
    }

    @Operation(summary = "Remove a role from a user", description = "Removes a role from a user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role removed",
                content = [Content(schema = Schema(implementation = UserRoleAssignmentResponse::class))]
            )
        ]
    )
    @DeleteMapping("/{pid}/roles/{roleId}")
    @RequiresPermission(AllPermissions.USER_ROLE_REMOVE)
    fun removeRoleFromUser(
        @PathVariable pid: String,
        @PathVariable roleId: Long,
    ): ResponseEntity<UserRoleAssignmentResponse> {
        val removedByPid = SecurityUtils.getCurrentUser()?.pid ?: throw IllegalArgumentException("User not found")
        return ResponseEntity.ok(
            userRoleService.removeRoleFromUser(
                RemoveRoleFromUserRequest(
                    userPid = pid,
                    roleId = roleId
                ), removedByPid
            )
        )
    }

    @Operation(summary = "Fetches permissions of current user.", description = "Fetches permissions of current user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Permissions of current user",
                content = [Content(schema = Schema(implementation = UserDetailsWithRolesAndPermissions::class))]
            )
        ]
    )
    @GetMapping("/me/{timeMillis}")
    fun getAllUsersWithRoles(
        @PathVariable timeMillis: Long,
    ): ResponseEntity<UserDetailsWithRolesAndPermissions> {
        val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("User not found")
        return ResponseEntity.ok(userRoleService.fetchDetailsOfUser(currentUser, timeMillis))
    }

    @Operation(
        summary = "Delete a user account",
        description = "Runs the same deletion as a person deleting their own account. " +
            "Personal details, including the roll number, are removed immediately and the " +
            "roll number is released. Attendance records are retained as de-identified rows " +
            "so past reports stay accurate, and the person renders as \"Former Volunteer\" " +
            "wherever they are still referenced. Sessions are revoked. This cannot be undone " +
            "and there is no restore. Recorded against the acting admin in account_deletions."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User deleted successfully",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    @DeleteMapping("/{pid}")
    @RequiresPermission(AllPermissions.USER_DELETE)
    fun deleteUser(@PathVariable pid: String): ResponseEntity<StringResponse> {
        val performedBy = SecurityUtils.getCurrentUser()
        val response = userService.deleteUser(pid, performedBy, DeletionSource.ADMIN)
        return ResponseEntity.ok(StringResponse(response.message))
    }

    @Operation(
        summary = "Delete your own account",
        description = "Permanently deletes the signed-in account. Personal details, including " +
            "the roll number, are removed immediately. Attendance records are retained as " +
            "de-identified rows so past reports stay accurate. This cannot be undone and " +
            "there is no restore; re-authentication is required."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Account deleted"),
            ApiResponse(responseCode = "401", description = "Re-authentication failed"),
            ApiResponse(responseCode = "400", description = "Refused: this is the only super admin account")
        ]
    )
    @DeleteMapping("/me")
    fun deleteMyAccount(@RequestBody request: DeleteMyAccountRequest): ResponseEntity<StringResponse> {
        val currentUser = SecurityUtils.getCurrentUser()
            ?: throw IllegalArgumentException("User not found")

        if (!authService.verifyReauthentication(currentUser, request.password, request.googleIdToken)) {
            throw BadCredentialsException("Please confirm your identity to delete your account.")
        }

        val response = userService.deleteUser(currentUser.pid, currentUser, DeletionSource.SELF)
        return ResponseEntity.ok(StringResponse(response.message))
    }
}