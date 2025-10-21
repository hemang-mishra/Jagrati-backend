package org.jagrati.jagratibackend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jagrati.jagratibackend.security.JWTService
import org.jagrati.jagratibackend.services.UserService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter

@Service
class JWTAuthenticationFilter(
    private val jwtService: JWTService,
    private val userService: UserService
): OncePerRequestFilter(){
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7) // Extract the token part after "Bearer "
                if (jwtService.validateAccessToken(token)) {
                    val pid = jwtService.getUserIdFromToken(token)
                    val user = userService.getUserById(pid)
                    if (user != null) {
                        val authentication = UsernamePasswordAuthenticationToken(
                            user,
                            null, // credentials - null as we're using token authentication
                            user.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
            filterChain.doFilter(request, response)
        }catch (ex: Exception){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }
}
