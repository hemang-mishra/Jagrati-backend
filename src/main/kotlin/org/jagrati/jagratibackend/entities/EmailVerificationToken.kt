package org.jagrati.jagratibackend.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "email_verification_token")
data class EmailVerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "token", nullable = false, unique = true)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant = Instant.now().plusSeconds(EMAIL_VERIFICATION_TOKEN_EXPIRY.toLong()),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    companion object {
        const val EMAIL_VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60 // 24 hours in seconds
    }
}