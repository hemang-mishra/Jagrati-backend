package org.jagrati.jagratibackend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(val email: String, val password: String, val deviceToken: String)
data class RefreshRequest(val refreshToken: String)
data class RegisterRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String
)

data class ResendVerificationRequest(val email: String)
data class ForgotPasswordRequest(val email: String)
data class GoogleLoginRequest(val idToken: String, val deviceToken: String)

