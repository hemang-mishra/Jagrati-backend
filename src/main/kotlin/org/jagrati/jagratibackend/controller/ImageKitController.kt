package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.dto.imagekit.ImageKitResponse
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.security.RequiresPermission
import org.jagrati.jagratibackend.services.ImageKitService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/image-kit")
class ImageKitController(
    private val imageKitService : ImageKitService
) {

    @RequiresPermission(AllPermissions.STUDENT_READ)
    @GetMapping
    fun getImageAuthToken(): ImageKitResponse {
        return imageKitService.getAuthenticationParameters()
    }
}