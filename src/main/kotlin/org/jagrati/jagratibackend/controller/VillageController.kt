package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.StringRequest
import org.jagrati.jagratibackend.services.VillageService
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
@RequestMapping("/api/village")
@Tag(name = "Village Management API", description = "Endpoints for managing villages")
class VillageController(
    val service: VillageService
) {
    @Operation(summary = "Add a village", description = "Creates a new village entry")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Village added successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.VILLAGE_MANAGE)
    @PostMapping("/add")
    fun addVillage(@RequestBody village: StringRequest): ResponseEntity<String> {
        service.addNewVillage(village)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "Remove a village", description = "Deletes a village by its ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Village removed successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.VILLAGE_MANAGE)
    @DeleteMapping("/remove")
    fun removeVillage(@RequestBody villageId: LongRequest): ResponseEntity<String> {
        service.deleteVillage(villageId)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "List active villages", description = "Fetches all active villages")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of active villages", content = [Content(schema = Schema(implementation = LongStringResponse::class))])
    ])
    @RequiresPermission(AllPermissions.VILLAGE_MANAGE)
    @GetMapping
    fun getAllActiveVillages(): ResponseEntity<List<LongStringResponse>> {
        return ResponseEntity.ok(service.getAllActiveVillages())
    }
}