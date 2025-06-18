package org.jagrati.jagratibackend.controller


import org.jagrati.jagratibackend.security.AuthService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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
}