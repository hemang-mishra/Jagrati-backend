package org.jagrati.jagratibackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.exception.GlobalExceptionHandler
import org.jagrati.jagratibackend.security.AuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@ExtendWith(SpringExtension::class)
@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.MockConfig::class, GlobalExceptionHandler::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authService: AuthService

    @Configuration
    class MockConfig {
        @Bean
        fun authService(): AuthService = Mockito.mock(AuthService::class.java)

        @Bean
        fun authController(authService: AuthService): AuthController {
            return AuthController(authService)
        }
    }

    @BeforeEach
    fun setup() {
        Mockito.reset(authService)
    }

    @Test
    fun `when login with valid credentials then return token pair`() {
        //Arrange
        val loginRequest = AuthController.LoginRequest(
            "test@jagrati",
            "password",
        )

        val tokenPair = AuthService.TokenPair(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        `when`(authService.login(loginRequest.email, loginRequest.password)).thenReturn(tokenPair)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(tokenPair.accessToken))
            .andExpect(jsonPath("$.refreshToken").value(tokenPair.refreshToken))

    }

    @Test
    fun `when login with invalid credentials then return unauthorized`() {
        //Arrange
        val loginRequest = AuthController.LoginRequest(
            "invalid@jagrati",
            "wrong-password",
        )

        `when`(authService.login(loginRequest.email, loginRequest.password))
            .thenThrow(RuntimeException("Invalid credentials"))

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when register with valid data then return user details`() {
        //Arrange
        val registerRequest = AuthController.RegisterRequest(
            email = "test@jagrati",
            password = "password",
            firstName = "firstName",
            lastName = "lastName",
        )

        val user = User(
            pid = "test-user",
            firstName = "firstName",
            lastName = "lastName",
            email = "test@jagrati",
            passwordHash = "test-hash",
        )

        `when`(
            authService.register(
                firstName = registerRequest.firstName,
                lastName = registerRequest.lastName,
                email = registerRequest.email,
                password = registerRequest.password
            )
        )
            .thenReturn(user)

        //Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.pid").value(user.pid))
            .andExpect(jsonPath("$.firstName").value(user.firstName))
            .andExpect(jsonPath("$.lastName").value(user.lastName))
            .andExpect(jsonPath("$.email").value(user.email))
    }

    @Test
    fun `when register with existing email then return bad request`() {
        //Arrange
        val registerRequest = AuthController.RegisterRequest(
            email = "existing@jagrati",
            password = "password",
            firstName = "firstName",
            lastName = "lastName",
        )

        `when`(
            authService.register(
                firstName = registerRequest.firstName,
                lastName = registerRequest.lastName,
                email = registerRequest.email,
                password = registerRequest.password
            )
        ).thenThrow(IllegalStateException("User with this email already exists"))

        //Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when refresh with valid token then return new token pair`() {
        //Arrange
        val refreshRequest = AuthController.RefreshRequest(
            refreshToken = "test-refresh-token",
        )
        val tokenPair = AuthService.TokenPair(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        `when`(authService.refresh(refreshRequest.refreshToken))
            .thenReturn(tokenPair)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(tokenPair.accessToken))
            .andExpect(jsonPath("$.refreshToken").value(tokenPair.refreshToken))
    }

    @Test
    fun `when refresh with invalid token then return unauthorized`() {
        //Arrange
        val refreshRequest = AuthController.RefreshRequest(
            refreshToken = "invalid-refresh-token",
        )

        `when`(authService.refresh(refreshRequest.refreshToken))
            .thenThrow(IllegalArgumentException("Invalid refresh token"))

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when resend verification with valid email then return success message`() {
        // Arrange
        val resendRequest = AuthController.ResendVerificationRequest(
            email = "user@example.com"
        )

        `when`(authService.resendVerificationEmail(resendRequest.email))
            .thenReturn(true)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Verification email sent"))

        verify(authService).resendVerificationEmail(resendRequest.email)
    }

    @Test
    fun `when resend verification for already verified email then return bad request`() {
        // Arrange
        val resendRequest = AuthController.ResendVerificationRequest(
            email = "verified@example.com"
        )

        `when`(authService.resendVerificationEmail(resendRequest.email))
            .thenThrow(IllegalStateException("Email already verified"))

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when resend verification for non-existent user then return not found`() {
        // Arrange
        val resendRequest = AuthController.ResendVerificationRequest(
            email = "nonexistent@example.com"
        )

        `when`(authService.resendVerificationEmail(resendRequest.email))
            .thenThrow(IllegalArgumentException("User not found"))

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `when resend verification fails then return bad request`() {
        // Arrange
        val resendRequest = AuthController.ResendVerificationRequest(
            email = "user@example.com"
        )

        `when`(authService.resendVerificationEmail(resendRequest.email))
            .thenReturn(false)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Failed to send verification email"))
    }
}
