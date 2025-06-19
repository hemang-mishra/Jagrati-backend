package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): PasswordResetToken?
    fun deleteByEmail(email: String)
    fun deleteByExpiresAtBefore(instant: Instant)
}
