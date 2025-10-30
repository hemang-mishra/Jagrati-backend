package org.jagrati.jagratibackend.utils

import org.jagrati.jagratibackend.entities.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Utility class for Spring Security operations.
 * Provides convenience methods for accessing the currently authenticated user.
 */
@Component
class SecurityUtils {
    companion object {
        /**
         * Get the currently authenticated user
         * @return the User object or null if not authenticated
         */
        fun getCurrentUser(): User? {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication.principal is User) {
                return authentication.principal as User
            }
            return null
        }
    }
}

object PidGenerator {
    fun generatePid(timeInMillis: Long = System.currentTimeMillis()): String{
        val uuid = UUID.randomUUID().toString()
        return "${uuid}_$timeInMillis"
    }
}