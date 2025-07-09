package org.jagrati.jagratibackend.controller

import ApproveVolunteerRequest
import CreateVolunteerRequest
import DetailedVolunteerRequestListResponse
import MyVolunteerRequestListResponse
import RejectVolunteerRequest
import VolunteerRequestActionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.*
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.services.VolunteerRequestService
import org.jagrati.jagratibackend.security.JWTService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/volunteer-requests")
@Tag(name = "Volunteer Request API", description = "Endpoints for managing volunteer requests")
class VolunteerRequestController(
    private val volunteerRequestService: VolunteerRequestService,
    private val jwtService: JWTService
) {
    @Operation(summary = "Create a volunteer request", description = "Creates a new volunteer request.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Request created", content = [Content(schema = Schema(implementation = VolunteerRequestActionResponse::class))])
    ])
    @PostMapping
    fun createVolunteerRequest(
        @RequestBody request: CreateVolunteerRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<VolunteerRequestActionResponse> {
        val userPid = jwtService.getUserIdFromToken(authHeader)
        return ResponseEntity.ok(volunteerRequestService.createVolunteerRequest(request, userPid))
    }

    @Operation(summary = "List all volunteer requests", description = "Fetches all volunteer requests with detailed information.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of detailed requests", content = [Content(schema = Schema(implementation = DetailedVolunteerRequestListResponse::class))])
    ])
    @GetMapping
    @RequiresPermission(AllPermissions.VOLUNTEER_REQUEST_VIEW)
    fun getAllVolunteerRequests(): ResponseEntity<DetailedVolunteerRequestListResponse> =
        ResponseEntity.ok(volunteerRequestService.getDetailedVolunteerRequests())

    @Operation(summary = "Approve a volunteer request", description = "Approves a volunteer request.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Request approved", content = [Content(schema = Schema(implementation = VolunteerRequestActionResponse::class))])
    ])
    @PostMapping("/{id}/approve")
    @RequiresPermission(AllPermissions.VOLUNTEER_REQUEST_APPROVE)
    fun approveVolunteerRequest(
        @PathVariable id: Long,
        @RequestBody request: ApproveVolunteerRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<VolunteerRequestActionResponse> {
        val approvedByPid = jwtService.getUserIdFromToken(authHeader)
        return ResponseEntity.ok(volunteerRequestService.approveVolunteerRequest(request.copy(requestId = id), approvedByPid))
    }

    @Operation(summary = "Reject a volunteer request", description = "Rejects a volunteer request.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Request rejected", content = [Content(schema = Schema(implementation = VolunteerRequestActionResponse::class))])
    ])
    @PostMapping("/{id}/reject")
    @RequiresPermission(AllPermissions.VOLUNTEER_REQUEST_REJECT)
    fun rejectVolunteerRequest(
        @PathVariable id: Long,
        @RequestBody request: RejectVolunteerRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<VolunteerRequestActionResponse> {
        val rejectedByPid = jwtService.getUserIdFromToken(authHeader)
        return ResponseEntity.ok(volunteerRequestService.rejectVolunteerRequest(request.copy(requestId = id), rejectedByPid))
    }

    @Operation(summary = "Get my volunteer requests", description = "Fetches all volunteer requests made by the signed-in user.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "My requests", content = [Content(schema = Schema(implementation = MyVolunteerRequestListResponse::class))])
    ])
    @GetMapping("/my")
    fun getMyVolunteerRequests(@RequestHeader("Authorization") authHeader: String): ResponseEntity<MyVolunteerRequestListResponse> {
        val userPid = jwtService.getUserIdFromToken(authHeader)
        return ResponseEntity.ok(volunteerRequestService.getMyVolunteerRequests(userPid))
    }
}