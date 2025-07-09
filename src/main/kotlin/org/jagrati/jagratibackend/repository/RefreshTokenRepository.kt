package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.RefreshTokens
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface RefreshTokenRepository: JpaRepository<RefreshTokens, Long> {
    fun deleteByExpiresAtBefore(instant: Instant)
    fun findByEmailAndHashedRefreshToken(email: String, refreshToken: String): RefreshTokens?
    fun deleteByEmailAndHashedRefreshToken(email: String, refreshToken: String)
}