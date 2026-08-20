package org.jagrati.jagratibackend.entities.enums

/**
 * Whether a volunteer record has an account attached to it.
 *
 * A PROVISIONAL volunteer is a real person, known by roll number, whose attendance
 * has been taken but who has never signed in. Their record is linked to an account
 * the moment someone signs in with the matching college address.
 */
enum class VolunteerStatus {
    PROVISIONAL,
    ACTIVE
}
