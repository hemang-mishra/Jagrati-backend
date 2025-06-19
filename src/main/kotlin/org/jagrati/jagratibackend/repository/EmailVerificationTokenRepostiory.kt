package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional


interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): EmailVerificationToken?

    @Modifying
    @Transactional
    fun deleteByEmail(email: String)

    fun findByEmail(email: String): EmailVerificationToken?
}