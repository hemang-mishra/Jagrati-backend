package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.RefreshTokens
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.List

interface RefreshTokenRepository: JpaRepository<RefreshTokens, Long> {
    fun deleteByExpiresAtBefore(instant: Instant)
    fun findByEmailAndHashedRefreshToken(email: String, refreshToken: String): RefreshTokens?
    fun deleteByEmailAndHashedRefreshToken(email: String, refreshToken: String)
    fun findAllByEmail(email: String): List<RefreshTokens>
}