package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.Volunteer
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.jagrati.jagratibackend.utils.PidGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Owns the link between a person and an account.
 *
 * Two entry points create or resolve a person:
 *
 *  - [findOrCreateByRollNumber] — someone is marked present by roll number. If no
 *    record exists we create a PROVISIONAL one. The person now exists in the system
 *    without ever having opened the app.
 *
 *  - [linkAccountOnSignIn] — someone signs in. Their verified college address gives
 *    a roll number; if a provisional record holds it, the account is attached to that
 *    row. Attendance already points at the row's pid, so the history transfers
 *    without moving anything.
 *
 * Linking is identity and happens at sign-in, because Google proved the address.
 * Granting the VOLUNTEER role is authorization and happens at approval. Keeping them
 * apart is what lets someone see their own history before they have any role.
 */
@Service
class VolunteerIdentityService(
    private val volunteerRepository: VolunteerRepository,
    private val userRepository: UserRepository,
    private val instituteIdentityService: InstituteIdentityService,
) {
    private val logger = LoggerFactory.getLogger(VolunteerIdentityService::class.java)

    /**
     * Resolve a roll number to a person, creating a provisional record if this is the
     * first time anyone has referred to them.
     *
     * [firstName]/[lastName] are hints from whoever is marking attendance. When absent
     * the roll number stands in as the display name, which is honest — it is genuinely
     * all we know about this person.
     */
    @Transactional
    fun findOrCreateByRollNumber(
        rawRollNumber: String,
        createdBy: User?,
        firstName: String? = null,
        lastName: String? = null,
        batch: String? = null,
    ): VolunteerLookup {
        val rollNumber = instituteIdentityService.normalizeRollNumber(rawRollNumber)
            ?: throw IllegalArgumentException("Roll number cannot be blank")

        volunteerRepository.findByRollNumberNormalizedAndDeletedAtIsNull(rollNumber)?.let {
            return VolunteerLookup(volunteer = it, created = false)
        }

        val created = volunteerRepository.save(
            Volunteer(
                pid = PidGenerator.generatePid(),
                rollNumber = rollNumber,
                rollNumberNormalized = rollNumber,
                user = null,
                status = VolunteerStatus.PROVISIONAL,
                createdBy = createdBy,
                firstName = firstName?.trim()?.ifEmpty { null } ?: rollNumber,
                lastName = lastName?.trim()?.ifEmpty { null } ?: "",
                gender = Gender.UNKNOWN,
                batch = batch?.trim()?.ifEmpty { null },
                dateOfBirth = null,
                isActive = true
            )
        )
        logger.info("Created provisional volunteer {} for roll number {}", created.pid, rollNumber)
        return VolunteerLookup(volunteer = created, created = true)
    }

    /**
     * Attach a signing-in account to the person record its address identifies.
     *
     * Returns the linked record, or null when the account proves no roll number (an
     * exempt account such as the seeded super admin) or when no record holds it yet.
     */
    @Transactional
    fun linkAccountOnSignIn(user: User): Volunteer? {
        val rollNumber = instituteIdentityService.deriveRollNumber(user.email) ?: return null

        volunteerRepository.findByUserPidAndDeletedAtIsNull(user.pid)?.let { existing ->
            // Backfill a roll number lost to the old no-fallback update bug. The V3
            // check constraint is NOT VALID precisely so these rows survive until here.
            if (existing.rollNumberNormalized == null) {
                if (volunteerRepository.existsByRollNumberNormalizedAndDeletedAtIsNull(rollNumber)) {
                    logger.warn(
                        "Cannot backfill roll number {} for volunteer {}: already held",
                        rollNumber, existing.pid
                    )
                    return existing
                }
                return volunteerRepository.save(
                    existing.copy(rollNumber = rollNumber, rollNumberNormalized = rollNumber)
                )
            }
            return existing
        }

        val provisional = volunteerRepository
            .findByRollNumberNormalizedAndDeletedAtIsNull(rollNumber)
            ?: return null

        if (provisional.user != null) {
            // Roll numbers derive from unique email addresses, so this should be
            // unreachable. Refuse rather than reassign if it ever happens.
            logger.error(
                "Roll number {} is already claimed by {}; refusing to relink to {}",
                rollNumber, provisional.user?.pid, user.pid
            )
            return null
        }

        val linked = volunteerRepository.save(provisional.copy(user = user))
        logger.info(
            "Linked account {} to provisional volunteer {} via roll number {}",
            user.pid, linked.pid, rollNumber
        )
        return linked
    }

    /**
     * Promote a linked record once a volunteer request is approved. Profile fields
     * supplied on the request fill in what a provisional record never knew.
     */
    @Transactional
    fun promoteToActive(volunteer: Volunteer, details: Volunteer.() -> Volunteer): Volunteer =
        volunteerRepository.save(volunteer.details().copy(status = VolunteerStatus.ACTIVE))

    fun findByUser(user: User): Volunteer? =
        volunteerRepository.findByUserPidAndDeletedAtIsNull(user.pid)

    fun findByRollNumber(rawRollNumber: String): Volunteer? =
        instituteIdentityService.normalizeRollNumber(rawRollNumber)
            ?.let { volunteerRepository.findByRollNumberNormalizedAndDeletedAtIsNull(it) }

    data class VolunteerLookup(
        val volunteer: Volunteer,
        /** True when this call brought the person into existence. */
        val created: Boolean
    )
}
