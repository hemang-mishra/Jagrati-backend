package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.AddFaceDataRequest
import org.jagrati.jagratibackend.dto.FaceDataResponse
import org.jagrati.jagratibackend.dto.StudentWithFaceDataResponse
import org.jagrati.jagratibackend.dto.UpdateFaceDataRequest
import org.jagrati.jagratibackend.dto.VolunteerWithFaceDataResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.services.FaceDataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/face-data")
class FaceDataController(private val faceDataService: FaceDataService) {
    @RequiresPermission(AllPermissions.FACE_AUTH_CREATE)
    @PostMapping
    fun add(@RequestBody request: AddFaceDataRequest): ResponseEntity<FaceDataResponse> =
        ResponseEntity.ok(faceDataService.addFaceData(request))

    @RequiresPermission(AllPermissions.FACE_AUTH_UPDATE)
    @PutMapping("/{pid}")
    fun update(@PathVariable pid: String, @RequestBody request: UpdateFaceDataRequest): ResponseEntity<FaceDataResponse> =
        ResponseEntity.ok(faceDataService.updateFaceData(pid, request))

    @RequiresPermission(AllPermissions.FACE_AUTH_DELETE)
    @DeleteMapping("/{pid}")
    fun delete(@PathVariable pid: String): ResponseEntity<Void> {
        faceDataService.deleteFaceData(pid)
        return ResponseEntity.ok().build()
    }

    @RequiresPermission(AllPermissions.FACE_AUTH_READ)
    @GetMapping("/{pid}")
    fun getByPid(@PathVariable pid: String): ResponseEntity<FaceDataResponse> =
        ResponseEntity.ok(faceDataService.getFaceDataByPid(pid))

    @RequiresPermission(AllPermissions.FACE_AUTH_READ)
    @GetMapping("/students")
    fun getAllStudents(): ResponseEntity<List<StudentWithFaceDataResponse>> =
        ResponseEntity.ok(faceDataService.getAllStudentsWithFaceData())

    @RequiresPermission(AllPermissions.FACE_AUTH_READ)
    @GetMapping("/volunteers")
    fun getAllVolunteers(): ResponseEntity<List<VolunteerWithFaceDataResponse>> =
        ResponseEntity.ok(faceDataService.getAllVolunteersWithFaceData())

    @PostMapping("/me")
    fun addForMe(@RequestBody request: UpdateFaceDataRequest): ResponseEntity<FaceDataResponse> =
        ResponseEntity.ok(faceDataService.addFaceDataForCurrentUser(request))

    @GetMapping("/me")
    fun getMyFaceData(): ResponseEntity<FaceDataResponse> =
        ResponseEntity.ok(faceDataService.getMyFaceData())
}
