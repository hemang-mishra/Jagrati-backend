package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.AttendanceReportResponse
import org.jagrati.jagratibackend.dto.AttendanceRecordResponse
import org.jagrati.jagratibackend.dto.BulkAttendanceRequest
import org.jagrati.jagratibackend.dto.BulkAttendanceResultResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.services.AttendanceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.IndividualAttendanceHistory

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Management API", description = "Endpoints for marking and reporting attendance")
class AttendanceController(private val attendanceService: AttendanceService) {
    @Operation(summary = "Mark student attendance in bulk")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Attendance marked", content = [Content(schema = Schema(implementation = BulkAttendanceResultResponse::class))])
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_MARK_STUDENT)
    @PostMapping("/students/mark-bulk")
    fun markStudents(@RequestBody request: BulkAttendanceRequest): ResponseEntity<BulkAttendanceResultResponse> =
        ResponseEntity.ok(attendanceService.markStudentAttendanceBulk(request))

    @Operation(summary = "Mark volunteer attendance in bulk")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Attendance marked", content = [Content(schema = Schema(implementation = BulkAttendanceResultResponse::class))])
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_MARK_VOLUNTEER)
    @PostMapping("/volunteers/mark-bulk")
    fun markVolunteers(@RequestBody request: BulkAttendanceRequest): ResponseEntity<BulkAttendanceResultResponse> =
        ResponseEntity.ok(attendanceService.markVolunteerAttendanceBulk(request))

    @Operation(summary = "Get daily attendance report")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Report fetched", content = [Content(schema = Schema(implementation = AttendanceReportResponse::class))])
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_READ)
    @GetMapping("/report")
    fun report(@RequestParam("date") date: String): ResponseEntity<AttendanceReportResponse> =
        ResponseEntity.ok(attendanceService.getAttendanceReport(date))

    @Operation(summary = "Delete student attendance by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Deleted")
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_DELETE_STUDENT)
    @DeleteMapping("/students/{id}")
    fun deleteStudentAttendance(@PathVariable id: Long): ResponseEntity<Void> {
        attendanceService.deleteStudentAttendanceById(id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Delete volunteer attendance by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Deleted")
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_DELETE_VOLUNTEER)
    @DeleteMapping("/volunteers/{id}")
    fun deleteVolunteerAttendance(@PathVariable id: Long): ResponseEntity<Void> {
        attendanceService.deleteVolunteerAttendanceById(id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Get a student's attendance records")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Records fetched", content = [Content(schema = Schema(implementation = AttendanceRecordResponse::class))])
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_READ)
    @GetMapping("/students/{pid}")
    fun getStudentAttendance(@PathVariable pid: String): ResponseEntity<IndividualAttendanceHistory> =
        ResponseEntity.ok(attendanceService.getStudentAttendanceByPid(pid))

    @Operation(summary = "Get a volunteer's attendance records")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Records fetched", content = [Content(schema = Schema(implementation = AttendanceRecordResponse::class))])
    ])
    @RequiresPermission(AllPermissions.ATTENDANCE_READ)
    @GetMapping("/volunteers/{pid}")
    fun getVolunteerAttendance(@PathVariable pid: String): ResponseEntity<IndividualAttendanceHistory> =
        ResponseEntity.ok(attendanceService.getVolunteerAttendanceByPid(pid))
}

