package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.MergeProvisionalVolunteerRequest
import org.jagrati.jagratibackend.dto.PersonMasking
import org.jagrati.jagratibackend.dto.ProvisionalVolunteerListResponse
import org.jagrati.jagratibackend.dto.ProvisionalVolunteerResponse
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
import org.jagrati.jagratibackend.repository.VolunteerAttendanceRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Cleanup for records created by typing a roll number.
 *
 * Marking attendance by roll number is open to anyone who can mark attendance, which
 * is deliberate — the person in the field has to be able to record who turned up. The
 * cost is that a mistyped roll number becomes a person. The autocomplete in the client
 * is the first line of defence; this is the second.
 */
@Service
class ProvisionalVolunteerService(
    private val volunteerRepository: VolunteerRepository,
    private val volunteerAttendanceRepository: VolunteerAttendanceRepository,
    private val instituteIdentityService: InstituteIdentityService,
) {
    private val logger = LoggerFactory.getLogger(ProvisionalVolunteerService::class.java)

    @Transactional(readOnly = true)
    fun listProvisional(): ProvisionalVolunteerListResponse {
        val volunteers = volunteerRepository.findAllByStatusAndDeletedAtIsNull(VolunteerStatus.PROVISIONAL)
        return ProvisionalVolunteerListResponse(
            volunteers.map { v ->
                ProvisionalVolunteerResponse(
                    pid = v.pid,
                    rollNumber = v.rollNumber,
                    firstName = v.firstName,
                    lastName = v.lastName,
                    batch = v.batch,
                    attendanceCount = volunteerAttendanceRepository.countByVolunteerPidPid(v.pid),
                    createdByPid = v.createdBy?.pid,
                    createdByName = PersonMasking.displayName(v.createdBy),
                    createdAt = v.createdAt.toString(),
                )
            }.sortedByDescending { it.createdAt }
        )
    }

    /**
     * Fold a mistyped record into the right one.
     *
     * Attendance moves across; a date the target already has is dropped rather than
     * duplicated, since being present twice on one day means nothing. Only unclaimed
     * records can be merged away — once an account is attached, the person themselves
     * is the only one who should be able to remove it.
     */
    @Transactional
    fun merge(request: MergeProvisionalVolunteerRequest): StringResponse {
        if (request.sourcePid == request.targetPid) {
            throw IllegalArgumentException("Cannot merge a record into itself")
        }

        val source = volunteerRepository.findByPidAndDeletedAtIsNull(request.sourcePid)
            ?: throw IllegalArgumentException("Source volunteer not found")
        val target = volunteerRepository.findByPidAndDeletedAtIsNull(request.targetPid)
            ?: throw IllegalArgumentException("Target volunteer not found")

        if (source.status != VolunteerStatus.PROVISIONAL || source.user != null) {
            throw IllegalStateException("Only an unclaimed provisional record can be merged away")
        }

        val targetDates = volunteerAttendanceRepository.findByVolunteerPidPid(target.pid)
            .map { it.attendanceDate }
            .toSet()

        var moved = 0
        var dropped = 0
        volunteerAttendanceRepository.findByVolunteerPidPid(source.pid).forEach { record ->
            if (record.attendanceDate in targetDates) {
                volunteerAttendanceRepository.delete(record)
                dropped += 1
            } else {
                volunteerAttendanceRepository.save(record.copy(volunteerPid = target))
                moved += 1
            }
        }
        volunteerAttendanceRepository.flush()
        volunteerRepository.delete(source)

        logger.info(
            "Merged provisional volunteer {} into {} ({} records moved, {} duplicates dropped)",
            source.pid, target.pid, moved, dropped
        )
        return StringResponse("Merged: $moved records moved, $dropped duplicates dropped")
    }

    /**
     * Volunteers still without a roll number, and therefore frozen.
     *
     * The V3 check constraint requires one on every living record, so a row that has
     * none cannot be updated at all. Login-time derivation clears most of these, but it
     * cannot help someone whose account is not on the institute domain — a pre-existing
     * external helper, say. Those rows need [assignRollNumber] or they stay stuck.
     */
    @Transactional(readOnly = true)
    fun listMissingRollNumber(): ProvisionalVolunteerListResponse =
        ProvisionalVolunteerListResponse(
            volunteerRepository.findAllMissingRollNumber().map { v ->
                ProvisionalVolunteerResponse(
                    pid = v.pid,
                    rollNumber = null,
                    firstName = v.firstName,
                    lastName = v.lastName,
                    batch = v.batch,
                    attendanceCount = volunteerAttendanceRepository.countByVolunteerPidPid(v.pid),
                    createdByPid = v.createdBy?.pid,
                    createdByName = PersonMasking.displayName(v.createdBy),
                    createdAt = v.createdAt.toString(),
                )
            }
        )

    /**
     * Set a roll number by hand, for a record that cannot derive one.
     *
     * The deliberate escape hatch from an otherwise fully derived scheme. Restricted to
     * records that have no roll number at all, so it can never be used to move one off
     * a person who already holds it.
     */
    @Transactional
    fun assignRollNumber(pid: String, rawRollNumber: String): StringResponse {
        val volunteer = volunteerRepository.findByPidAndDeletedAtIsNull(pid)
            ?: throw IllegalArgumentException("Volunteer not found")

        if (volunteer.rollNumberNormalized != null) {
            throw IllegalStateException(
                "This volunteer already has a roll number. It is derived from their college " +
                    "address and cannot be reassigned."
            )
        }

        val rollNumber = instituteIdentityService.normalizeRollNumber(rawRollNumber)
            ?: throw IllegalArgumentException("Roll number cannot be blank")

        if (volunteerRepository.existsByRollNumberNormalizedAndDeletedAtIsNull(rollNumber)) {
            throw IllegalStateException("Roll number $rollNumber already belongs to someone else.")
        }

        volunteerRepository.save(
            volunteer.copy(rollNumber = rollNumber, rollNumberNormalized = rollNumber)
        )
        logger.info("Assigned roll number {} to volunteer {} by hand", rollNumber, pid)
        return StringResponse("Roll number set to $rollNumber")
    }

    /** Remove a record created by mistake that has no attendance worth keeping. */
    @Transactional
    fun remove(pid: String): StringResponse {
        val volunteer = volunteerRepository.findByPidAndDeletedAtIsNull(pid)
            ?: throw IllegalArgumentException("Volunteer not found")

        if (volunteer.status != VolunteerStatus.PROVISIONAL || volunteer.user != null) {
            throw IllegalStateException("Only an unclaimed provisional record can be removed")
        }

        val records = volunteerAttendanceRepository.findByVolunteerPidPid(pid)
        records.forEach { volunteerAttendanceRepository.delete(it) }
        volunteerAttendanceRepository.flush()
        volunteerRepository.delete(volunteer)

        logger.info("Removed provisional volunteer {} and {} attendance records", pid, records.size)
        return StringResponse("Removed record and ${records.size} attendance entries")
    }
}
