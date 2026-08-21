package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.enums.Gender

data class BulkAttendanceRequest(
    val date: String,
    val pids: List<String> = emptyList(),

    /**
     * Roll numbers of people who may not be registered on the app.
     *
     * A roll number that resolves to nobody creates a provisional volunteer record, so
     * someone who volunteers without ever installing the app still gets credited — and
     * when they do sign in, the history is already theirs.
     */
    val rollNumbers: List<String> = emptyList()
)

data class MarkByRollNumberRequest(
    val date: String,
    val rollNumber: String,
    /** Optional hints from whoever is marking; the roll number stands in when absent. */
    val firstName: String? = null,
    val lastName: String? = null,
    val batch: String? = null,
)

data class RollNumberAttendanceResult(
    val rollNumber: String,
    val pid: String,
    val marked: Boolean,
    /** True when this call brought the person into existence. */
    val createdProvisionalRecord: Boolean,
    val message: String,
)

data class RollNumberLookupResponse(
    val rollNumber: String,
    val exists: Boolean,
    val pid: String?,
    val displayName: String?,
    val batch: String?,
    val isProvisional: Boolean,
)

data class BulkAttendanceResultResponse(
    val date: String,
    val totalRequested: Int,
    val inserted: Int,
    val skippedExisting: Int,
    val missingPids: List<String>,
    val rollNumberResults: List<RollNumberAttendanceResult> = emptyList()
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
    val batch: String?,
    val rollNo: String
)

data class AttendanceReportResponse(
    val date: String,
    val studentsByVillageGender: List<StudentVillageGenderCount>,
    val volunteersByBatch: List<VolunteerBatchCount>,
    val presentStudents: List<PresentStudent>,
    val presentVolunteers: List<PresentVolunteer>
)

data class IndividualAttendanceHistory(
    val attendees: List<AttendanceRecordResponse>,
)