package org.jagrati.jagratibackend.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The derivation and normalization rules are load-bearing: a roll number typed during
 * attendance and one derived from an address must produce the same string, or a person
 * marked present will never link to their own account.
 */
class InstituteIdentityServiceTest {

    private val service = InstituteIdentityService(
        instituteDomain = "iiitdmj.ac.in",
        enforceDomain = true,
        allowedNonInstituteEmailsRaw = "admin@jagrati.com"
    )

    @Test
    fun `derives the roll number from the local part`() {
        assertEquals("21BCS001", service.deriveRollNumber("21bcs001@iiitdmj.ac.in"))
    }

    @Test
    fun `derivation is case and whitespace insensitive`() {
        assertEquals("21BCS001", service.deriveRollNumber("  21BCS001@IIITDMJ.AC.IN  "))
    }

    @Test
    fun `a typed roll number normalizes to the same string as a derived one`() {
        val derived = service.deriveRollNumber("21bcs001@iiitdmj.ac.in")
        assertEquals(derived, service.normalizeRollNumber("21bcs001"))
        assertEquals(derived, service.normalizeRollNumber(" 21 BCS 001 "))
        assertEquals(derived, service.normalizeRollNumber("21BCS001"))
    }

    @Test
    fun `a non-institute address proves no roll number`() {
        assertNull(service.deriveRollNumber("helper@gmail.com"))
        assertNull(service.deriveRollNumber("admin@jagrati.com"))
    }

    @Test
    fun `a lookalike domain is not the institute domain`() {
        assertFalse(service.isInstituteEmail("someone@notiiitdmj.ac.in.evil.com"))
        assertNull(service.deriveRollNumber("someone@notiiitdmj.ac.in.evil.com"))
    }

    @Test
    fun `roll numbers are case-insensitive and canonicalise to uppercase`() {
        assertEquals("21BCS001", service.normalizeRollNumber("21bcs001"))
        assertEquals("21BCS001", service.normalizeRollNumber("21BcS001"))
        assertEquals(
            service.normalizeRollNumber("21bcs001"),
            service.normalizeRollNumber("21BCS001")
        )
    }

    @Test
    fun `invisible characters from a pasted roll number are stripped`() {
        // Must match RollNumbersTest on the Android side exactly. Java's \s is
        // ASCII-only and misses these; a roll number pasted from a web page, a PDF or a
        // chat message carries them, and one is enough to create a second person for
        // the same human.
        assertEquals("21BCS001", service.normalizeRollNumber("21BCS\u00A0001"))
        assertEquals("21BCS001", service.normalizeRollNumber("\u00A021BCS001\u00A0"))
        assertEquals("21BCS001", service.normalizeRollNumber("21BCS\u200B001"))
        assertEquals("21BCS001", service.normalizeRollNumber("21BCS\uFEFF001"))
    }

    @Test
    fun `a pasted address still derives the same roll number`() {
        assertEquals("21BCS001", service.deriveRollNumber("\u00A021bcs001@iiitdmj.ac.in\u00A0"))
    }

    @Test
    fun `uppercasing does not depend on the default locale`() {
        val default = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"))
            assertEquals("IIITDM001", service.normalizeRollNumber("iiitdm001"))
        } finally {
            java.util.Locale.setDefault(default)
        }
    }

    @Test
    fun `blank roll numbers normalize to null rather than empty`() {
        assertNull(service.normalizeRollNumber(null))
        assertNull(service.normalizeRollNumber(""))
        assertNull(service.normalizeRollNumber("   "))
    }

    @Test
    fun `sign-in is allowed for institute addresses and the exempt list only`() {
        assertTrue(service.isSignInAllowed("21bcs001@iiitdmj.ac.in"))
        assertTrue(service.isSignInAllowed("admin@jagrati.com"))
        assertFalse(service.isSignInAllowed("helper@gmail.com"))
    }

    @Test
    fun `enforcement can be turned off without affecting derivation`() {
        val relaxed = InstituteIdentityService(
            instituteDomain = "iiitdmj.ac.in",
            enforceDomain = false,
            allowedNonInstituteEmailsRaw = ""
        )
        assertTrue(relaxed.isSignInAllowed("helper@gmail.com"))
        // Still proves no roll number: relaxing who may sign in must not invent identity.
        assertNull(relaxed.deriveRollNumber("helper@gmail.com"))
    }

    @Test
    fun `a roll number longer than the column is truncated rather than failing the insert`() {
        val long = "A".repeat(60)
        assertEquals(InstituteIdentityService.MAX_ROLL_NUMBER_LENGTH, service.normalizeRollNumber(long)?.length)
    }
}
