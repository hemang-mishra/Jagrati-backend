package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.UpdateVolunteerRequest
import org.jagrati.jagratibackend.dto.VolunteerResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.services.VolunteerService
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/api/volunteers")
@Tag(name = "Volunteer API", description = "Endpoints for listing and fetching volunteers")
class VolunteerController(
    private val volunteerService: VolunteerService
) {
    @Operation(summary = "List all volunteers")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of volunteers", content = [Content(schema = Schema(implementation = VolunteerResponse::class))])
    ])
    @RequiresPermission(AllPermissions.USER_VIEW)
    @GetMapping
    fun getAllVolunteers(): ResponseEntity<List<VolunteerResponse>> =
        ResponseEntity.ok(volunteerService.getAllVolunteers())

    @Operation(summary = "Get volunteer by pid")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Volunteer details", content = [Content(schema = Schema(implementation = VolunteerResponse::class))])
    ])
    @RequiresPermission(AllPermissions.USER_VIEW)
    @GetMapping("/{pid}")
    fun getVolunteerByPid(@PathVariable pid: String): ResponseEntity<VolunteerResponse> =
        ResponseEntity.ok(volunteerService.getVolunteerByPid(pid))

    @Operation(summary = "Update current user's volunteer details")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Updated volunteer details", content = [Content(schema = Schema(implementation = VolunteerResponse::class))]),
        ApiResponse(responseCode = "404", description = "Volunteer not found"),
        ApiResponse(responseCode = "401", description = "Unauthorized")
    ])
    @PutMapping("/update-my-details")
    fun updateMyVolunteerDetails(
        @RequestBody updateRequest: UpdateVolunteerRequest
    ): ResponseEntity<VolunteerResponse> {
        val currentUser = SecurityUtils.getCurrentUser()
            ?: throw IllegalStateException("User not authenticated")

        val updatedVolunteer = volunteerService.updateVolunteerDetails(currentUser.pid, updateRequest)
        return ResponseEntity.ok(updatedVolunteer)
    }
}
