package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.StudentRequest
import org.jagrati.jagratibackend.dto.UpdateStudentRequest
import org.jagrati.jagratibackend.dto.StudentGroupHistoryResponse
import org.jagrati.jagratibackend.dto.StudentResponse
import org.jagrati.jagratibackend.services.StudentDetailsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.dto.StudentGroupHistoryListResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management API", description = "Endpoints for managing student details and group history")
class StudentController(
    private val studentDetailsService: StudentDetailsService
) {
    @Operation(summary = "Register a new student", description = "Creates a new student and records initial group assignment")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Student registered successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.STUDENT_CREATE)
    @PostMapping("/register")
    fun register(@RequestBody request: StudentRequest): ResponseEntity<String> {
        studentDetailsService.registerNewStudent(request)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "Update student details", description = "Updates student information and records group change if groupId changes")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Student updated successfully", content = [Content(schema = Schema(implementation = String::class))])
    ])
    @RequiresPermission(AllPermissions.STUDENT_UPDATE)
    @PutMapping("/update")
    fun update(@RequestBody request: UpdateStudentRequest): ResponseEntity<String> {
        studentDetailsService.updateStudent(request)
        return ResponseEntity.ok("Success")
    }

    @Operation(summary = "Get group transitions", description = "Returns the student's group assignment history (most recent first)")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Group history list", content = [Content(schema = Schema(implementation = StudentGroupHistoryResponse::class))])
    ])
    @RequiresPermission(AllPermissions.STUDENT_READ)
    @GetMapping("/{pid}/group-history")
    fun getGroupHistory(@PathVariable pid: String): ResponseEntity<StudentGroupHistoryListResponse> =
        ResponseEntity.ok(studentDetailsService.getGroupTransitions(pid))

    @Operation(summary = "List all students")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of students", content = [Content(schema = Schema(implementation = StudentResponse::class))])
    ])
    @RequiresPermission(AllPermissions.STUDENT_READ)
    @GetMapping
    fun getAllStudents(): ResponseEntity<List<StudentResponse>> =
        ResponseEntity.ok(studentDetailsService.getAllStudents())

    @Operation(summary = "Get student by pid")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Student details", content = [Content(schema = Schema(implementation = StudentResponse::class))])
    ])
    @RequiresPermission(AllPermissions.STUDENT_READ)
    @GetMapping("/{pid}")
    fun getStudentByPid(@PathVariable pid: String): ResponseEntity<StudentResponse> =
        ResponseEntity.ok(studentDetailsService.getStudentByPid(pid))

    @Operation(summary = "Delete student by pid", description = "Soft deletes a student by anonymizing their data")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Student deleted successfully", content = [Content(schema = Schema(implementation = String::class))]),
        ApiResponse(responseCode = "404", description = "Student not found")
    ])
    @RequiresPermission(AllPermissions.STUDENT_DELETE)
    @DeleteMapping("/{pid}")
    fun deleteStudent(@PathVariable pid: String): ResponseEntity<StringResponse> {
        val response = studentDetailsService.deleteStudent(pid)
        return ResponseEntity.ok(response)
    }
}
