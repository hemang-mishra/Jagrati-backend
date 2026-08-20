package org.jagrati.jagratibackend.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jagrati.jagratibackend.dto.*
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
import org.jagrati.jagratibackend.entities.StudentAttendance
import org.jagrati.jagratibackend.entities.VolunteerAttendance
import org.jagrati.jagratibackend.repository.FCMTokensRepository
import org.jagrati.jagratibackend.repository.StudentAttendanceRepository
import org.jagrati.jagratibackend.repository.StudentRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.VolunteerAttendanceRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.jagrati.jagratibackend.utils.NotificationContent
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AttendanceService(
    private val studentRepository: StudentRepository,
    private val studentAttendanceRepository: StudentAttendanceRepository,
    private val volunteerRepository: VolunteerRepository,
    private val volunteerAttendanceRepository: VolunteerAttendanceRepository,
    private val fcmTokenRepository: FCMTokensRepository,
    private val fcmService: FCMService,
    private val userRepository: UserRepository,
    private val volunteerIdentityService: VolunteerIdentityService,
    private val instituteIdentityService: InstituteIdentityService,
) {
    @Transactional
    fun markStudentAttendanceBulk(request: BulkAttendanceRequest): BulkAttendanceResultResponse {
        val date = LocalDate.parse(request.date)
        val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("No current user")
        val students = studentRepository.findAllById(request.pids)
        val foundPids = students.map { it.pid }.toSet()
        val missing = request.pids.filter { it !in foundPids }
        var inserted = 0
        var skippedExisting = 0
        students.forEach { s ->
            if (studentAttendanceRepository.existsByStudentIdPidAndDate(s.pid, date)) {
                skippedExisting += 1
            } else {
                try {
                    studentAttendanceRepository.save(
                        StudentAttendance(
                            date = date,
                            studentId = s,
                            markedByVolunteer = currentUser,
                            remarks = null
                        )
                    )
                    inserted += 1
                } catch (ex: DataIntegrityViolationException) {
                    skippedExisting += 1
                }
            }
        }
        return BulkAttendanceResultResponse(
            date = date.toString(),
            totalRequested = request.pids.size,
            inserted = inserted,
            skippedExisting = skippedExisting,
            missingPids = missing
        )
    }

    @Transactional
    fun markVolunteerAttendanceBulk(request: BulkAttendanceRequest): BulkAttendanceResultResponse {
        val date = LocalDate.parse(request.date)
        val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("No current user")
        val volunteers = volunteerRepository.findAllById(request.pids)
        val foundPids = volunteers.map { it.pid }.toSet()
        val missing = request.pids.filter { it !in foundPids }
        var inserted = 0
        var skippedExisting = 0
        volunteers.forEach { v ->
            if (volunteerAttendanceRepository.existsByVolunteerPidPidAndAttendanceDate(v.pid, date)) {
                skippedExisting += 1
            } else {
                try {
                    volunteerAttendanceRepository.save(
                        VolunteerAttendance(
                            volunteerPid = v,
                            markedBy = currentUser,
                            attendanceDate = date,
                            remarks = null
                        )
                    )
                    inserted += 1
                    // A provisional volunteer has no account to notify.
                    v.user?.let { user ->
                        fcmService.sendNotificationToMultipleDevices(listOf(user), NotificationContent.APPRECIATION_FOR_VOLUNTEERING)
                    }
                } catch (ex: DataIntegrityViolationException) {
                    skippedExisting += 1
                }
            }
        }
        val rollNumberResults = request.rollNumbers.map { rollNumber ->
            markOneByRollNumber(rollNumber, date, currentUser)
        }
        inserted += rollNumberResults.count { it.marked }
        skippedExisting += rollNumberResults.count { !it.marked && it.pid.isNotEmpty() }

        return BulkAttendanceResultResponse(
            date = date.toString(),
            totalRequested = request.pids.size + request.rollNumbers.size,
            inserted = inserted,
            skippedExisting = skippedExisting,
            missingPids = missing,
            rollNumberResults = rollNumberResults
        )
    }

    /**
     * Mark someone present by roll number, whether or not they have ever opened the app.
     *
     * An unrecognised roll number is not an error — it is the common case this exists
     * for. It creates a provisional person record so the session is credited now, and
     * the record links itself to their account the first time they sign in.
     */
    @Transactional
    fun markVolunteerAttendanceByRollNumber(request: MarkByRollNumberRequest): RollNumberAttendanceResult {
        val date = LocalDate.parse(request.date)
        val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("No current user")
        return markOneByRollNumber(
            rawRollNumber = request.rollNumber,
            date = date,
            currentUser = currentUser,
            firstName = request.firstName,
            lastName = request.lastName,
            batch = request.batch,
        )
    }

    /**
     * Does a roll number already belong to someone? Drives the autocomplete that keeps
     * a typo from silently becoming a person.
     */
    @Transactional(readOnly = true)
    fun lookupRollNumber(rawRollNumber: String): RollNumberLookupResponse {
        val normalized = instituteIdentityService.normalizeRollNumber(rawRollNumber)
            ?: throw IllegalArgumentException("Roll number cannot be blank")
        val volunteer = volunteerIdentityService.findByRollNumber(normalized)
        return RollNumberLookupResponse(
            rollNumber = normalized,
            exists = volunteer != null,
            pid = volunteer?.pid,
            displayName = volunteer?.let { "${it.firstName} ${it.lastName}".trim() },
            batch = volunteer?.batch,
            isProvisional = volunteer?.status == VolunteerStatus.PROVISIONAL,
        )
    }

    private fun markOneByRollNumber(
        rawRollNumber: String,
        date: LocalDate,
        currentUser: org.jagrati.jagratibackend.entities.User,
        firstName: String? = null,
        lastName: String? = null,
        batch: String? = null,
    ): RollNumberAttendanceResult {
        val normalized = instituteIdentityService.normalizeRollNumber(rawRollNumber)
            ?: return RollNumberAttendanceResult(
                rollNumber = rawRollNumber,
                pid = "",
                marked = false,
                createdProvisionalRecord = false,
                message = "Roll number cannot be blank"
            )

        val lookup = volunteerIdentityService.findOrCreateByRollNumber(
            rawRollNumber = normalized,
            createdBy = currentUser,
            firstName = firstName,
            lastName = lastName,
            batch = batch,
        )
        val volunteer = lookup.volunteer

        if (volunteerAttendanceRepository.existsByVolunteerPidPidAndAttendanceDate(volunteer.pid, date)) {
            return RollNumberAttendanceResult(
                rollNumber = normalized,
                pid = volunteer.pid,
                marked = false,
                createdProvisionalRecord = lookup.created,
                message = "Already marked present on this date"
            )
        }

        return try {
            volunteerAttendanceRepository.save(
                VolunteerAttendance(
                    volunteerPid = volunteer,
                    markedBy = currentUser,
                    attendanceDate = date,
                    remarks = null
                )
            )
            volunteer.user?.let { user ->
                fcmService.sendNotificationToMultipleDevices(
                    listOf(user),
                    NotificationContent.APPRECIATION_FOR_VOLUNTEERING
                )
            }
            RollNumberAttendanceResult(
                rollNumber = normalized,
                pid = volunteer.pid,
                marked = true,
                createdProvisionalRecord = lookup.created,
                message = if (lookup.created) "Marked present; new record created" else "Marked present"
            )
        } catch (ex: DataIntegrityViolationException) {
            RollNumberAttendanceResult(
                rollNumber = normalized,
                pid = volunteer.pid,
                marked = false,
                createdProvisionalRecord = lookup.created,
                message = "Already marked present on this date"
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAttendanceReport(dateStr: String): AttendanceReportResponse {
        val date = LocalDate.parse(dateStr)
        val studentRecords = studentAttendanceRepository.findByDate(date)
        val volunteerRecords = volunteerAttendanceRepository.findByAttendanceDate(date)
        val studentsByVillageGender = studentRecords.groupBy { Pair(it.studentId.village.id, it.studentId.gender) }
            .map { (k, list) ->
                StudentVillageGenderCount(
                    villageId = k.first,
                    villageName = list.first().studentId.village.name,
                    gender = k.second,
                    count = list.size.toLong()
                )
            }
        val volunteersByBatch = volunteerRecords.groupBy { it.volunteerPid.batch }
            .map { (batch, list) ->
                VolunteerBatchCount(batch = batch, count = list.size.toLong())
            }
        val presentStudents = studentRecords.map {
            val s = it.studentId
            val masked = s.isDeleted
            PresentStudent(
                pid = s.pid,
                firstName = if (masked) PersonMasking.DELETED_FIRST_NAME else s.firstName,
                lastName = if (masked) PersonMasking.DELETED_STUDENT_LAST_NAME else s.lastName,
                gender = s.gender,
                villageId = s.village.id,
                villageName = s.village.name,
                groupId = s.group.id,
                groupName = s.group.name,
                aid = it.id
            )
        }
        val presentVolunteers = volunteerRecords.map {
            val v = it.volunteerPid
            val masked = v.isDeleted
            PresentVolunteer(
                pid = v.pid,
                firstName = if (masked) PersonMasking.DELETED_FIRST_NAME else v.firstName,
                lastName = if (masked) PersonMasking.DELETED_LAST_NAME else v.lastName,
                batch = if (masked) null else v.batch,
                aid = it.id,
                rollNo = if (masked) "" else v.rollNumber ?: ""
            )
        }
        return AttendanceReportResponse(
            date = date.toString(),
            studentsByVillageGender = studentsByVillageGender,
            volunteersByBatch = volunteersByBatch,
            presentStudents = presentStudents,
            presentVolunteers = presentVolunteers
        )
    }

    @Transactional
    fun deleteStudentAttendanceById(id: Long) {
        if (!studentAttendanceRepository.existsById(id)) throw IllegalArgumentException("Student attendance not found")
        studentAttendanceRepository.deleteById(id)
    }

    @Transactional
    fun deleteVolunteerAttendanceById(id: Long) {
        if (!volunteerAttendanceRepository.existsById(id)) throw IllegalArgumentException("Volunteer attendance not found")
        volunteerAttendanceRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getStudentAttendanceByPid(pid: String): IndividualAttendanceHistory {
        return IndividualAttendanceHistory(
        studentAttendanceRepository.findByStudentIdPid(pid)
            .sortedByDescending { it.date }
            .map { AttendanceRecordResponse(id = it.id, date = it.date.toString(), remarks = it.remarks) })
    }

    @Transactional(readOnly = true)
    fun getVolunteerAttendanceByPid(pid: String): IndividualAttendanceHistory {
        return IndividualAttendanceHistory(volunteerAttendanceRepository.findByVolunteerPidPid(pid)
            .sortedByDescending { it.attendanceDate }
            .map { AttendanceRecordResponse(id = it.id, date = it.attendanceDate.toString(), remarks = it.remarks) })
    }
}
