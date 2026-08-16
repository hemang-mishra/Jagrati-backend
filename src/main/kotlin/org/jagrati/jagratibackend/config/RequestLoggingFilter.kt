package org.jagrati.jagratibackend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jagrati.jagratibackend.entities.User
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val requestLogger = LoggerFactory.getLogger(javaClass)

    private fun extractClientIp(request: HttpServletRequest): String {
        request.getHeader("X-Forwarded-For")?.let { xff ->
            if (xff.isNotBlank()) return xff.split(",").first().trim()
        }
        request.getHeader("X-Real-IP")?.let { xr ->
            if (xr.isNotBlank()) return xr.trim()
        }
        request.getHeader("Forwarded")?.let { fwd ->
            val match = Regex("for=([^;,\"]+)").find(fwd)
            if (match != null) return match.groupValues[1]
        }
        return request.remoteAddr ?: "unknown"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - start
            val principal = SecurityContextHolder.getContext().authentication?.principal
            val identity = (principal as? User)?.let { "${it.email} (${it.pid})" } ?: "anonymous"
            val query = request.queryString?.let { "?$it" } ?: ""
            requestLogger.info(
                "{} {}{} - user={} ip={} status={} duration={}ms",
                request.method,
                request.requestURI,
                query,
                identity,
                extractClientIp(request),
                response.status,
                duration
            )
        }
    }
}
