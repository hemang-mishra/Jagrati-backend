package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.enums.Gender

data class BulkAttendanceRequest(
    val date: String,
    val pids: List<String>
)

data class BulkAttendanceResultResponse(
    val date: String,
    val totalRequested: Int,
    val inserted: Int,
    val skippedExisting: Int,
    val missingPids: List<String>
)

data class AttendanceRecordResponse(
    val id: Long,
    val date: String,
    val remarks: String?
)

data class StudentVillageGenderCount(
    val villageId: Long,
    val villageName: String,
    val gender: Gender,
    val count: Long
)

data class VolunteerBatchCount(
    val batch: String?,
    val count: Long
)

data class PresentStudent(
    val pid: String,
    val aid: Long,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val villageId: Long,
    val villageName: String,
    val groupId: Long,
    val groupName: String
)

data class PresentVolunteer(
    val pid: String,
    val aid: Long,
    val firstName: String,
    val lastName: String,
    val batch: String?
)

data class AttendanceReportResponse(
    val date: String,
    val studentsByVillageGender: List<StudentVillageGenderCount>,
    val volunteersByBatch: List<VolunteerBatchCount>,
    val presentStudents: List<PresentStudent>,
    val presentVolunteers: List<PresentVolunteer>
)

