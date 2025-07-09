package org.jagrati.jagratibackend.controller

import AssignRoleToPermissionRequest
import PermissionListResponse
import PermissionResponse
import PermissionRoleAssignmentResponse
import PermissionWithRolesListResponse
import RemoveRoleFromPermissionRequest
import RoleSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.services.PermissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permission Management API", description = "Endpoints for managing permissions")
class PermissionController(private val permissionService: PermissionService) {

    @Operation(summary = "List all permissions", description = "Fetches all permissions.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of permissions", content = [Content(schema = Schema(implementation = PermissionListResponse::class))])
    ])
    @GetMapping
    @RequiresPermission(AllPermissions.PERMISSION_VIEW)
    fun getAllPermissions(): ResponseEntity<PermissionListResponse> =
        ResponseEntity.ok(permissionService.getAllPermissions())

    @Operation(summary = "Get permission details", description = "Fetches details of a specific permission.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Permission details", content = [Content(schema = Schema(implementation = PermissionResponse::class))])
    ])
    @GetMapping("/{id}")
    @RequiresPermission(AllPermissions.PERMISSION_VIEW)
    fun getPermissionById(@PathVariable id: Long): ResponseEntity<PermissionResponse> =
        ResponseEntity.ok(permissionService.getPermissionById(id))

    @Operation(summary = "Assign a role to a permission", description = "Assigns a role to a permission.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Role assigned", content = [Content(schema = Schema(implementation = PermissionRoleAssignmentResponse::class))])
    ])
    @PostMapping("/{id}/roles")
    @RequiresPermission(AllPermissions.PERMISSION_ASSIGN_ROLE)
    fun assignRoleToPermission(@PathVariable id: Long, @RequestBody request: AssignRoleToPermissionRequest): ResponseEntity<PermissionRoleAssignmentResponse> =
        ResponseEntity.ok(permissionService.assignRoleToPermission(request.copy(permissionId = id)))

    @Operation(summary = "Remove a role from a permission", description = "Removes a role from a permission.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Role removed", content = [Content(schema = Schema(implementation = PermissionRoleAssignmentResponse::class))])
    ])
    @DeleteMapping("/{id}/roles/{roleId}")
    @RequiresPermission(AllPermissions.PERMISSION_REMOVE_ROLE)
    fun removeRoleFromPermission(@PathVariable id: Long, @PathVariable roleId: Long): ResponseEntity<PermissionRoleAssignmentResponse> =
        ResponseEntity.ok(permissionService.removeRoleFromPermission(RemoveRoleFromPermissionRequest(permissionId = id, roleId = roleId)))

    @Operation(summary = "List all permissions with assigned roles", description = "Fetches all permissions with their assigned roles.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Permissions with roles", content = [Content(schema = Schema(implementation = PermissionWithRolesListResponse::class))])
    ])
    @GetMapping("/with-roles")
    @RequiresPermission(AllPermissions.PERMISSION_VIEW)
    fun getAllPermissionsWithRoles(): ResponseEntity<PermissionWithRolesListResponse> =
        ResponseEntity.ok(permissionService.getAllPermissionsWithRoles())

    @Operation(summary = "Get all roles assigned to a permission", description = "Fetches all roles assigned to a specific permission.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of roles", content = [Content(schema = Schema(implementation = RoleSummaryResponse::class))])
    ])
    @GetMapping("/{id}/roles")
    @RequiresPermission(AllPermissions.PERMISSION_VIEW)
    fun getRolesForPermission(@PathVariable id: Long): ResponseEntity<List<RoleSummaryResponse>> =
        ResponseEntity.ok(permissionService.getRolesForPermission(id))
}