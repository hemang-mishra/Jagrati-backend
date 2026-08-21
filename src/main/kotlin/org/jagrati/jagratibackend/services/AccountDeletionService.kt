package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.config.InitialRoles
import org.jagrati.jagratibackend.entities.AccountDeletion
import org.jagrati.jagratibackend.entities.ImageKitResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.enums.DeletionSource
import org.jagrati.jagratibackend.repository.AccountDeletionRepository
import org.jagrati.jagratibackend.repository.FCMTokensRepository
import org.jagrati.jagratibackend.repository.RefreshTokenRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.jagrati.jagratibackend.repository.VolunteerRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Deletion: soft at the row, final for the person.
 *
 * The rows survive, because attendance, audit trails and every "marked by" reference
 * point at them and the counts must stay right. The personal data on them does not:
 * name, email, roll number, contact details, address and photo are scrubbed here and
 * now. There is no grace period and no restore.
 *
 * Releasing the roll number is the part that decides the semantics. It is personal
 * data in its own right, so it goes — and because the uniqueness index only binds
 * living rows, releasing it means a returning person is correctly treated as new
 * rather than inheriting a record they asked to be rid of.
 */
@Service
class AccountDeletionService(
    private val userRepository: UserRepository,
    private val volunteerRepository: VolunteerRepository,
    private val volunteerRequestRepository: VolunteerRequestRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val fcmTokensRepository: FCMTokensRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
    private val accountDeletionRepository: AccountDeletionRepository,
    private val imageKitService: ImageKitService,
    private val fcmService: FCMService,
) {
    private val logger = LoggerFactory.getLogger(AccountDeletionService::class.java)

    @Transactional
    fun deleteAccount(pid: String, performedBy: User?, source: DeletionSource): DeletionOutcome {
        val user = userRepository.findByPidAndDeletedAtIsNull(pid)
            ?: throw IllegalArgumentException("User not found")

        guardLastSuperAdmin(user)

        val now = LocalDateTime.now()
        val volunteer = volunteerRepository.findByUserPidAndDeletedAtIsNull(pid)

        // Revoke access first. If anything below fails the transaction rolls back, but
        // an interrupted deletion should never leave a usable session behind.
        refreshTokenRepository.findAllByEmail(user.email).forEach { refreshTokenRepository.delete(it) }
        fcmTokensRepository.findByUser(user).forEach { fcmTokensRepository.delete(it) }
        refreshTokenRepository.flush()
        fcmTokensRepository.flush()

        volunteer?.let { v ->
            ImageKitResponse.getFromString(v.profilePicDetails)?.fileId?.let { fileId ->
                runCatching { imageKitService.deleteFile(fileId) }
                    .onFailure { logger.warn("Failed to delete profile picture from ImageKit: ${it.message}") }
            }

            volunteerRepository.save(
                v.copy(
                    rollNumber = null,
                    rollNumberNormalized = null,
                    firstName = SCRUBBED_FIRST_NAME,
                    lastName = SCRUBBED_LAST_NAME,
                    alternateEmail = null,
                    batch = null,
                    programme = null,
                    streetAddress1 = null,
                    streetAddress2 = null,
                    pincode = null,
                    city = null,
                    state = null,
                    dateOfBirth = null,
                    contactNumber = null,
                    college = null,
                    branch = null,
                    yearOfStudy = null,
                    profilePicDetails = null,
                ).apply {
                    isActive = false
                    deletedAt = now
                    deletedByPid = performedBy?.pid
                    deletionSource = source
                }
            )
        }

        volunteerRequestRepository.findByRequestedByAndDeletedAtIsNull(user).forEach { request ->
            volunteerRequestRepository.save(
                request.copy(
                    rollNumber = null,
                    rollNumberNormalized = null,
                    firstName = SCRUBBED_FIRST_NAME,
                    lastName = SCRUBBED_LAST_NAME,
                    alternateEmail = null,
                    batch = null,
                    programme = null,
                    streetAddress1 = null,
                    streetAddress2 = null,
                    pincode = null,
                    city = null,
                    state = null,
                    contactNumber = null,
                    college = null,
                    branch = null,
                    yearOfStudy = null,
                ).also {
                    it.reason = null
                    it.deletedAt = now
                }
            )
        }

        // The email is scrubbed to a value derived from the pid: it must stay unique,
        // and freeing the real address is what lets the same human sign up fresh later.
        userRepository.save(
            user.copy(
                firstName = SCRUBBED_FIRST_NAME,
                lastName = SCRUBBED_LAST_NAME,
                email = "${user.pid}@deleted.invalid",
                passwordHash = "",
                profilePictureUrl = null,
            ).apply {
                isActive = false
                isEmailVerified = false
                deletedAt = now
                deletedByPid = performedBy?.pid
                deletionSource = source
            }
        )

        accountDeletionRepository.save(
            AccountDeletion(
                subjectPid = pid,
                performedByPid = performedBy?.pid,
                source = source,
                hadVolunteerRecord = volunteer != null
            )
        )

        fcmService.sendSyncNotification()
        logger.info("Deleted account {} (source={}, volunteer={})", pid, source, volunteer != null)

        return DeletionOutcome(pid = pid, hadVolunteerRecord = volunteer != null)
    }

    /**
     * Refuse to remove the last way into the system. Without this, a super admin
     * deleting their own account locks everyone out with no recovery path — and since
     * deletion is final, there is nothing to restore.
     */
    private fun guardLastSuperAdmin(user: User) {
        val superAdminRole = roleRepository.findByName(InitialRoles.SUPER_ADMIN.roleString) ?: return
        val holdsRole = userRoleRepository.findByUserAndRole(user, superAdminRole) != null
        if (!holdsRole) return

        val remaining = userRoleRepository.findByRole(superAdminRole)
            .count { it.user.pid != user.pid && !it.user.isDeleted }

        if (remaining == 0) {
            throw IllegalStateException(
                "This is the only super admin account. Grant the role to someone else before deleting it."
            )
        }
    }

    data class DeletionOutcome(val pid: String, val hadVolunteerRecord: Boolean)

    companion object {
        const val SCRUBBED_FIRST_NAME = "Former"
        const val SCRUBBED_LAST_NAME = "Volunteer"
    }
}
