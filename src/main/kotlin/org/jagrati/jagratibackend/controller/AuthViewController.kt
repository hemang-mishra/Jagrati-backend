package org.jagrati.jagratibackend.controller


import org.jagrati.jagratibackend.services.AuthService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/auth")
class AuthViewController(
    private val authService: AuthService,
) {
    @GetMapping("/verify-email")
    fun verifyEmail(@RequestParam token: String, model: Model): String {
        return try {
            authService.verifyEmail(token)
            "verification-success"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", e.message ?: "Verification failed")
            "verification-error"
        }
    }

    @GetMapping("/reset-password")
    fun showResetPasswordForm(@RequestParam token: String, model: Model): String {
        return try {
            val isTokenValid = authService.validateResetToken(token)
            if (isTokenValid) {
                model.addAttribute("token", token)
                "reset-password-form"
            } else {
                model.addAttribute("errorMessage", "Invalid or expired password reset token")
                "reset-password-error"
            }
        } catch (e: Exception) {
            model.addAttribute("errorMessage", e.message ?: "Invalid or expired password reset token")
            "reset-password-error"
        }
    }

    @PostMapping("/reset-password")
    fun processResetPassword(
        @RequestParam token: String,
        @RequestParam password: String,
        model: Model
    ): String {
        return try {
            val success = authService.resetPassword(token, password)
            if (success) {
                "reset-password-success"
            } else {
                model.addAttribute("errorMessage", "Failed to reset password")
                "reset-password-error"
            }
        } catch (e: Exception) {
            model.addAttribute("errorMessage", e.message ?: "Password reset failed")
            "reset-password-error"
        }
    }
}