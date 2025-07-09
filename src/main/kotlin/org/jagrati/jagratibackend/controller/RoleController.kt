package org.jagrati.jagratibackend.controller

import CreateRoleRequest
import DeactivateRoleRequest
import RoleListResponse
import RoleResponse
import UpdateRoleRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.*
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.services.RoleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management API", description = "Endpoints for managing roles")
class RoleController(private val roleService: RoleService) {

    @Operation(summary = "List all roles", description = "Fetches all roles.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of roles",
                content = [Content(schema = Schema(implementation = RoleListResponse::class))]
            )
        ]
    )
    @GetMapping
    @RequiresPermission(AllPermissions.ROLE_VIEW)
    fun getAllRoles(): ResponseEntity<RoleListResponse> =
        ResponseEntity.ok(roleService.getAllRoles())

    @Operation(summary = "Create a new role", description = "Creates a new role.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role created",
                content = [Content(schema = Schema(implementation = RoleResponse::class))]
            )
        ]
    )
    @PostMapping
    @RequiresPermission(AllPermissions.ROLE_CREATE)
    fun createRole(@RequestBody request: CreateRoleRequest): ResponseEntity<RoleResponse> =
        ResponseEntity.ok(roleService.createRole(request))

    @Operation(summary = "Get role details", description = "Fetches details of a specific role.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role details",
                content = [Content(schema = Schema(implementation = RoleResponse::class))]
            )
        ]
    )
    @GetMapping("/{id}")
    @RequiresPermission(AllPermissions.ROLE_VIEW)
    fun getRoleById(@PathVariable id: Long): ResponseEntity<RoleResponse> =
        ResponseEntity.ok(roleService.getRoleById(id))

    @Operation(summary = "Update a role", description = "Updates an existing role.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role updated",
                content = [Content(schema = Schema(implementation = RoleResponse::class))]
            )
        ]
    )
    @PutMapping("/{id}")
    @RequiresPermission(AllPermissions.ROLE_UPDATE)
    fun updateRole(@PathVariable id: Long, @RequestBody request: UpdateRoleRequest): ResponseEntity<RoleResponse> =
        ResponseEntity.ok(roleService.updateRole(request.copy(id = id)))

    @Operation(summary = "Deactivate a role", description = "Deactivates (soft-deletes) a role.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Role deactivated",
                content = [Content(schema = Schema(implementation = RoleResponse::class))]
            )
        ]
    )
    @PatchMapping("/{id}/deactivate")
    @RequiresPermission(AllPermissions.ROLE_DEACTIVATE)
    fun deactivateRole(@PathVariable id: Long): ResponseEntity<RoleResponse> =
        ResponseEntity.ok(roleService.deactivateRole(DeactivateRoleRequest(id)))
}