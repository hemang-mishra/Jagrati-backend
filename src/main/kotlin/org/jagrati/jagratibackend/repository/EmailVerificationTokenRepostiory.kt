package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository


interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): EmailVerificationToken?
    fun deleteByEmail(email: String)
    fun findByEmail(email: String): EmailVerificationToken?
}