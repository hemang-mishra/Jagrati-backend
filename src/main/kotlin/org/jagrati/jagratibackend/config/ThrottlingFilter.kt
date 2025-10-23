package org.jagrati.jagratibackend.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log

@Component
class ThrottlingFilter: OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val buckets : ConcurrentHashMap<String, Bucket > = ConcurrentHashMap()

    private fun getBucket(key: String): Bucket {
        return buckets.computeIfAbsent(key) {
            Bucket.builder().addLimit(){
                it.capacity(30)
                    .refillGreedy(20, Duration.ofMinutes(1))
            }
                .build()
        }
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        request.getHeader("X-Forwarded-For")?.let { xff ->
            if (xff.isNotBlank()) return xff.split(",").first().trim()
        }
        request.getHeader("X-Real-IP")?.let { xr ->
            if (xr.isNotBlank()) return xr.trim()
        }
        request.getHeader("Forwarded")?.let { fwd ->
            // header format: Forwarded: for=1.2.3.4;proto=http
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
        val clientIP = extractClientIp(request)
//        logger.info("Client IP: $clientIP")
        val bucket = getBucket(clientIP)
//        logger.info("Bucket: ${bucket.availableTokens}")
        if(bucket.tryConsume(1L)){
            filterChain.doFilter(request, response)
        } else {
            response.status = 429
            response.writer.write("Too many requests - Rate limit exceeded")
        }
    }
}