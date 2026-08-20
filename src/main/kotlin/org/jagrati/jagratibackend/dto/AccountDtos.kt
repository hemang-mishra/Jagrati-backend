package org.jagrati.jagratibackend.dto

/**
 * Re-authentication for a destructive, irreversible action. Supply whichever the
 * account has: a password, or a fresh Google ID token for accounts that never chose one.
 */
data class DeleteMyAccountRequest(
    val password: String? = null,
    val googleIdToken: String? = null,
)

data class ProvisionalVolunteerResponse(
    val pid: String,
    val rollNumber: String?,
    val firstName: String,
    val lastName: String,
    val batch: String?,
    val attendanceCount: Long,
    val createdByPid: String?,
    val createdByName: String,
    val createdAt: String,
)

data class ProvisionalVolunteerListResponse(
    val volunteers: List<ProvisionalVolunteerResponse>
)

data class MergeProvisionalVolunteerRequest(
    /** The record created by mistake; its attendance moves to [targetPid]. */
    val sourcePid: String,
    val targetPid: String,
)

/** Admin escape hatch for a record that cannot derive a roll number from its address. */
data class AssignRollNumberRequest(
    val rollNumber: String,
)
