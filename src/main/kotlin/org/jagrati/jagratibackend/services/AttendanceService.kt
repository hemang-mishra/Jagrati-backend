package org.jagrati.jagratibackend.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jagrati.jagratibackend.dto.*
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
    private val userRepository: UserRepository
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
                    val user = userRepository.findByPid(v.pid)
                    if (user != null) {
                        fcmService.sendNotificationToMultipleDevices(listOf(user), NotificationContent.APPRECIATION_FOR_VOLUNTEERING)
                    }
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
            PresentStudent(
                pid = s.pid,
                firstName = s.firstName,
                lastName = s.lastName,
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
            PresentVolunteer(
                pid = v.pid,
                firstName = v.firstName,
                lastName = v.lastName,
                batch = v.batch,
                aid = it.id,
                rollNo = v.rollNumber ?: ""
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
