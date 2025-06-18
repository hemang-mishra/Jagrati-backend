package org.jagrati.jagratibackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/test")
class TestController {

    @GetMapping("/secured")
    fun securedEndpoint(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication

        return ResponseEntity.ok(
            mapOf(
                "message" to "This is a secured endpoint that requires a valid JWT token",
                "timestamp" to LocalDateTime.now().toString(),
                "authenticated" to true,
                "username" to authentication.name,
                "authorities" to authentication.authorities.map { it.authority }
            )
        )
    }

    @GetMapping("/hello")
    fun hello(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "Hello from the test controller!",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }
}
