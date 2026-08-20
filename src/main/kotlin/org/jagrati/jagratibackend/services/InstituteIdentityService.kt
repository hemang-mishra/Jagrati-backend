package org.jagrati.jagratibackend.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Turns a verified email address into a roll number.
 *
 * The local part of an institute address *is* the roll number, so an address proven
 * by Google sign-in proves the roll number too. That is what lets the app stop asking
 * users to type it, and what makes claiming a provisional record safe without an
 * admin adjudicating the claim.
 *
 * Everything here works from the address the *server* verified, never from a value
 * supplied by the client.
 */
@Service
class InstituteIdentityService(
    @param:Value("\${app.institute.email-domain}")
    private val instituteDomain: String,

    @param:Value("\${app.institute.enforce-email-domain:true}")
    private val enforceDomain: Boolean,

    @param:Value("\${app.institute.allowed-non-institute-emails:}")
    private val allowedNonInstituteEmailsRaw: String,
) {
    private val allowedNonInstituteEmails: Set<String> by lazy {
        allowedNonInstituteEmailsRaw.split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private val suffix: String get() = "@${instituteDomain.lowercase()}"

    fun isInstituteEmail(email: String): Boolean =
        email.trim().lowercase().endsWith(suffix)

    fun isExempt(email: String): Boolean =
        email.trim().lowercase() in allowedNonInstituteEmails

    /**
     * Whether an account may be created or signed in with this address.
     *
     * Enforced here rather than in the client. The Android check
     * (`Utils.isCollegeEmailId`) is a courtesy message; this is the rule.
     */
    fun isSignInAllowed(email: String): Boolean =
        !enforceDomain || isInstituteEmail(email) || isExempt(email)

    /**
     * The roll number this address proves, or null for an address that proves none
     * (an exempt account such as the seeded super admin). A null result means the
     * account cannot hold a volunteer record.
     */
    fun deriveRollNumber(email: String): String? {
        if (!isInstituteEmail(email)) return null
        val localPart = email.trim().substringBefore('@')
        return normalizeRollNumber(localPart)
    }

    /**
     * Canonical form for comparison and storage.
     *
     * Both the derived path and any hand-typed path must produce the same string, or
     * a person marked present by roll number will not link to their own account.
     */
    fun normalizeRollNumber(raw: String?): String? {
        val trimmed = raw?.trim()?.uppercase()?.replace(WHITESPACE, "") ?: return null
        return trimmed.ifEmpty { null }?.take(MAX_ROLL_NUMBER_LENGTH)
    }

    companion object {
        const val MAX_ROLL_NUMBER_LENGTH = 32
        private val WHITESPACE = Regex("\\s+")
    }
}
