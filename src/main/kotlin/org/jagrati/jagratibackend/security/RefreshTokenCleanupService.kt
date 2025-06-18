package org.jagrati.jagratibackend.security

import org.jagrati.jagratibackend.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenCleanupService(private val refreshTokenRepository: RefreshTokenRepository) {
    @Scheduled(fixedRate = 24* 60 * 60 * 1000)
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now())
    }
}