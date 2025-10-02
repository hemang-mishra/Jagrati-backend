package org.jagrati.jagratibackend.utils

import org.jagrati.jagratibackend.entities.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

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
    fun generatePid(timeInMillis: Long = System.currentTimeMillis(), name: String): String{
        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9]"), "_")
        return "${sanitizedName}_$timeInMillis"
    }
}