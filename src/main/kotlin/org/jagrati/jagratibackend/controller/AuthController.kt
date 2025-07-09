package org.jagrati.jagratibackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jagrati.jagratibackend.dto.*
import org.jagrati.jagratibackend.security.AuthService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication API", description = "Endpoints for user authentication and account management"
)
class AuthController(
    private val authService: AuthService, @Value("\${app.base-url}") private val baseUrl: String
) {

    @Operation(
        summary = "Register a new user", description = "Registers a new user and returns the created user details."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "201", description = "User registered successfully", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = RegisterResponse::class)
            )]
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid input or user already exists",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/register")
    fun register(
        @RequestBody body: RegisterRequest,
    ): ResponseEntity<RegisterResponse> {
        val user = authService.register(
            email = body.email, password = body.password, firstName = body.firstName, lastName = body.lastName
        )
        val response = RegisterResponse(
            pid = user.pid, firstName = user.firstName, lastName = user.lastName, email = user.email
        )
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @Operation(
        summary = "Login user", description = "Authenticates a user and returns a JWT token pair."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Login successful", content = [Content(
                mediaType = "application/json"
            )]
        ), ApiResponse(
            responseCode = "401", description = "Invalid credentials", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/login")
    @ResponseBody
    fun login(
        @RequestBody body: LoginRequest
    ): ResponseEntity<AuthService.TokenPair> {
        val tokenPair = authService.login(body.email, body.password)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }

    @Operation(
        summary = "Refresh JWT token", description = "Refreshes the JWT token pair using a valid refresh token."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Token refreshed successfully", content = [Content(
                mediaType = "application/json"
            )]
        ), ApiResponse(
            responseCode = "401", description = "Invalid or expired refresh token", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/refresh")
    @ResponseBody
    fun refresh(
        @RequestBody body: RefreshRequest
    ): ResponseEntity<AuthService.TokenPair> {
        val tokenPair = authService.refresh(body.refreshToken)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }


    @Operation(
        summary = "Resend verification email", description = "Resends a verification email to the user."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Verification email sent", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        ), ApiResponse(
            responseCode = "400", description = "Failed to send verification email", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/resend-verification")
    fun resendVerification(@RequestBody request: ResendVerificationRequest): ResponseEntity<MessageResponse> {
        val emailSent = authService.resendVerificationEmail(request.email)
        return if (emailSent) {
            ResponseEntity.ok(MessageResponse("Verification email sent"))
        } else {
            ResponseEntity.badRequest().body(MessageResponse("Failed to send verification email"))
        }
    }

    @Operation(
        summary = "Forgot password", description = "Initiates password reset process."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Password reset email sent", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        ), ApiResponse(
            responseCode = "400", description = "Failed to send password reset email", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        val emailSent = authService.initiatePasswordReset(request.email)
        return if (emailSent) {
            ResponseEntity.ok(MessageResponse("Password reset email has been sent"))
        } else {
            ResponseEntity.badRequest().body(MessageResponse("Failed to send password reset email"))
        }
    }

    @Operation(
        summary = "Get Google login URL", description = "Returns the Google OAuth2 login URL."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Google login URL", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = GoogleLoginUrlResponse::class)
            )]
        )]
    )
    @GetMapping("/google-login-url")
    fun googleLoginUrl(): ResponseEntity<GoogleLoginUrlResponse> {
        val url = "$baseUrl/oauth2/authorize/google"
        return ResponseEntity.ok(GoogleLoginUrlResponse(url))
    }

    @Operation(
        summary = "Google login for Android",
        description = "Authenticates a user using Google ID token and returns a JWT token pair."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Login successful", content = [Content(
                mediaType = "application/json"
            )]
        ), ApiResponse(
            responseCode = "401", description = "Invalid Google ID token", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = MessageResponse::class)
            )]
        )]
    )
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<AuthService.TokenPair> {
        val tokenPair = authService.loginWithGoogle(request.idToken)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }
}