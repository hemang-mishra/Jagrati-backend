package org.jagrati.jagratibackend.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
data class RefreshTokens(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val email: String,
    val hashedRefreshToken: String,
    val expiresAt: Instant,
): BaseEntity(){
    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())
}
