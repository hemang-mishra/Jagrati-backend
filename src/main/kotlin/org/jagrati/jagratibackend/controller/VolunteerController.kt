package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.UpdateVolunteerRequest
import org.jagrati.jagratibackend.dto.VolunteerResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.dto.AssignRollNumberRequest
import org.jagrati.jagratibackend.dto.MergeProvisionalVolunteerRequest
import org.jagrati.jagratibackend.dto.ProvisionalVolunteerListResponse
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.services.ProvisionalVolunteerService
import org.jagrati.jagratibackend.services.VolunteerService
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.DeleteMapping
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
    private val volunteerService: VolunteerService,
    private val provisionalVolunteerService: ProvisionalVolunteerService,
) {
    @Operation(
        summary = "List provisional volunteers",
        description = "People known only by roll number, whose attendance has been taken " +
            "but who have never signed in. Review here to catch records created by a mistyped roll number."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Provisional records", content = [Content(schema = Schema(implementation = ProvisionalVolunteerListResponse::class))])
    ])
    @RequiresPermission(AllPermissions.USER_VIEW)
    @GetMapping("/provisional")
    fun listProvisional(): ResponseEntity<ProvisionalVolunteerListResponse> =
        ResponseEntity.ok(provisionalVolunteerService.listProvisional())

    @Operation(
        summary = "Merge a provisional record into another",
        description = "Moves attendance from a mistyped record onto the right one. Dates the " +
            "target already has are dropped rather than duplicated."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Merged"),
        ApiResponse(responseCode = "400", description = "Refused: the source record has an account attached")
    ])
    @RequiresPermission(AllPermissions.USER_DELETE)
    @PostMapping("/provisional/merge")
    fun mergeProvisional(@RequestBody request: MergeProvisionalVolunteerRequest): ResponseEntity<StringResponse> =
        ResponseEntity.ok(provisionalVolunteerService.merge(request))

    @Operation(
        summary = "List volunteers with no roll number",
        description = "Records that cannot derive a roll number from their account address " +
            "and are therefore frozen until one is assigned by hand."
    )
    @RequiresPermission(AllPermissions.GROUP_MANAGE_VOLUNTEERS)
    @GetMapping("/missing-roll-number")
    fun listMissingRollNumber(): ResponseEntity<ProvisionalVolunteerListResponse> =
        ResponseEntity.ok(provisionalVolunteerService.listMissingRollNumber())

    @Operation(
        summary = "Assign a roll number by hand",
        description = "Only for a record that has none. A roll number already held is derived " +
            "from a verified college address and is never reassigned."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Roll number set"),
        ApiResponse(responseCode = "400", description = "Refused: already has one, or the number is taken")
    ])
    @RequiresPermission(AllPermissions.GROUP_MANAGE_VOLUNTEERS)
    @PutMapping("/{pid}/roll-number")
    fun assignRollNumber(
        @PathVariable pid: String,
        @RequestBody request: AssignRollNumberRequest
    ): ResponseEntity<StringResponse> =
        ResponseEntity.ok(provisionalVolunteerService.assignRollNumber(pid, request.rollNumber))

    @Operation(summary = "Remove a provisional record created by mistake")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Removed"),
        ApiResponse(responseCode = "400", description = "Refused: the record has an account attached")
    ])
    @RequiresPermission(AllPermissions.USER_DELETE)
    @DeleteMapping("/provisional/{pid}")
    fun removeProvisional(@PathVariable pid: String): ResponseEntity<StringResponse> =
        ResponseEntity.ok(provisionalVolunteerService.remove(pid))

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
