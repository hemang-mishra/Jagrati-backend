package org.jagrati.jagratibackend.controller

import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.security.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    data class LoginRequest(val email: String, val password: String)
    data class RefreshRequest(val refreshToken: String)
    data class RegisterRequest(val email: String, val password: String, val firstName: String, val lastName: String)
    data class RegisterResponse(val pid: String, val firstName: String, val lastName: String, val email: String)

    @PostMapping("/register")
    fun register(
        @RequestBody body: RegisterRequest,
    ): ResponseEntity<RegisterResponse> {
        val user = authService.register(
            email = body.email,
            password = body.password,
            firstName = body.firstName,
            lastName = body.lastName
        )
        val response = RegisterResponse(
            pid = user.pid,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email
        )
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @PostMapping("/login")
    @ResponseBody
    fun login(
        @RequestBody body: LoginRequest
    ): ResponseEntity<AuthService.TokenPair> {
        val tokenPair = authService.login(body.email, body.password)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }

    @PostMapping("/refresh")
    @ResponseBody
    fun refresh(
        @RequestBody body: RefreshRequest
    ): ResponseEntity<AuthService.TokenPair> {
        val tokenPair = authService.refresh(body.refreshToken)
        return ResponseEntity(tokenPair, HttpStatus.OK)
    }
}