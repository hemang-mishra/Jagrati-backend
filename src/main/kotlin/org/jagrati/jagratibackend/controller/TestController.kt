package org.jagrati.jagratibackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.HelloResponse
import org.jagrati.jagratibackend.dto.SecuredResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "Endpoints for testing authentication and basic functionality")
class TestController {

    @Operation(
        summary = "Get secured information",
        description = "This endpoint requires a valid JWT token and returns authenticated user details"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved authentication information",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = SecuredResponse::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token is missing or invalid",
            content = [Content(mediaType = "application/json")]
        )
    ])
    @GetMapping("/secured")
    fun securedEndpoint(): ResponseEntity<SecuredResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        return ResponseEntity.ok(
            SecuredResponse(
                message = "This is a secured endpoint that requires a valid JWT token",
                timestamp = LocalDateTime.now().toString(),
                authenticated = true,
                username = authentication.name,
                authorities = authentication.authorities.map { it.authority }
            )
        )
    }

    @Operation(
        summary = "Test hello endpoint",
        description = "A simple endpoint that returns a greeting message and current timestamp"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved greeting message",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = HelloResponse::class)
            )]
        )
    ])
    @GetMapping("/hello")
    fun hello(): ResponseEntity<HelloResponse> {
        return ResponseEntity.ok(
            HelloResponse(
                message = "Hello from the test controller!",
                timestamp = LocalDateTime.now().toString()
            )
        )
    }
}
