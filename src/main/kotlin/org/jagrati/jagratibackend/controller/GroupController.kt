package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.NameDescriptionRequest
import org.jagrati.jagratibackend.services.GroupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Group Management API", description = "Endpoints for managing student groups")
class GroupController(
    private val service: GroupService
) {
    @Operation(summary = "Create a group", description = "Creates a new group with name and optional description")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Group created successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.GROUP_MANAGE)
    @PostMapping("/add")
    fun addGroup(@RequestBody request: NameDescriptionRequest): ResponseEntity<String> {
        service.addNewGroup(request)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "Deactivate a group", description = "Soft-deletes a group by setting isActive to false")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Group deactivated successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.GROUP_MANAGE)
    @DeleteMapping("/remove")
    fun removeGroup(@RequestBody request: LongRequest): ResponseEntity<String> {
        service.deleteGroup(request)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "List active groups", description = "Fetches all active groups")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of active groups", content = [Content(schema = Schema(implementation = LongStringResponse::class))])
    ])
    @GetMapping("/getAll")
    fun getAllActiveGroups(): ResponseEntity<List<LongStringResponse>> {
        return ResponseEntity.ok(service.getAllActiveGroups())
    }
}
