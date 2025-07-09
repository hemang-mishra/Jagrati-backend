package org.jagrati.jagratibackend.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val token: String,
    val email: String,
    val expiresAt: Instant = Instant.now().plusSeconds(3600), // 1 hour expiration
    val createdAt: Instant = Instant.now()
)
