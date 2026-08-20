package org.jagrati.jagratibackend.dto

import UserSummaryDTO
import org.jagrati.jagratibackend.entities.Student
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.Volunteer
import org.jagrati.jagratibackend.entities.ImageKitResponse

/**
 * How a deleted person appears to everyone else.
 *
 * Deletion scrubs the columns it can, but a person's name also reaches clients
 * through live joins that no scrub can reach — `markedBy` on an attendance row,
 * `assignedBy` on a role, `registeredBy` on a student, `requestedBy` on a volunteer
 * request. Those all resolve the retained row, so masking has to happen where the
 * response is built rather than where the data is stored.
 *
 * Every mapper that renders a person routes through here, so there is one definition
 * of what "hidden" means instead of one per call site.
 */
object PersonMasking {

    const val DELETED_FIRST_NAME = "Former"
    const val DELETED_LAST_NAME = "Volunteer"
    const val DELETED_STUDENT_LAST_NAME = "Student"

    fun maskedName(isStudent: Boolean = false): Pair<String, String> =
        DELETED_FIRST_NAME to if (isStudent) DELETED_STUDENT_LAST_NAME else DELETED_LAST_NAME

    fun User.toSummary(): UserSummaryDTO =
        if (isDeleted) {
            val (first, last) = maskedName()
            UserSummaryDTO(
                pid = pid,
                firstName = first,
                lastName = last,
                email = "",
                profileImageUrl = null
            )
        } else {
            UserSummaryDTO(
                pid = pid,
                firstName = firstName,
                lastName = lastName,
                email = email,
                profileImageUrl = profilePictureUrl
            )
        }

    /**
     * A deleted volunteer keeps their pid — attendance rows point at it and the counts
     * must stay right — and loses everything that identifies them, roll number included.
     */
    fun Volunteer.toMaskedResponse(): VolunteerResponse {
        if (!isDeleted) return toResponse()
        val (first, last) = maskedName()
        return VolunteerResponse(
            pid = pid,
            userPid = null,
            status = status.name,
            rollNumber = null,
            firstName = first,
            lastName = last,
            gender = gender,
            alternateEmail = null,
            batch = null,
            programme = null,
            streetAddress1 = null,
            streetAddress2 = null,
            pincode = null,
            city = null,
            state = null,
            dateOfBirth = "",
            contactNumber = null,
            college = null,
            branch = null,
            yearOfStudy = null,
            isActive = false,
            profilePic = null
        )
    }

    fun Student.toMaskedResponse(): StudentResponse {
        if (!isDeleted) return toResponse()
        val (first, last) = maskedName(isStudent = true)
        return StudentResponse(
            pid = pid,
            firstName = first,
            lastName = last,
            yearOfBirth = null,
            gender = gender,
            profilePic = null as ImageKitResponse?,
            schoolClass = null,
            villageId = village.id,
            villageName = village.name,
            groupId = group.id,
            groupName = group.name,
            primaryContactNo = null,
            secondaryContactNo = null,
            fathersName = null,
            mothersName = null,
            isActive = false
        )
    }

    /** Display name for a person referenced from a retained record. */
    fun displayName(user: User?): String = when {
        user == null -> "Unknown"
        user.isDeleted -> "${DELETED_FIRST_NAME} ${DELETED_LAST_NAME}"
        else -> "${user.firstName} ${user.lastName}"
    }
}
