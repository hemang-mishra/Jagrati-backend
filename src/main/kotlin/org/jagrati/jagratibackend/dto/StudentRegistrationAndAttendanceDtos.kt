package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.ImageKitResponse

data class StudentRequest(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val yearOfBirth: Int? = null,
    val gender: String,
    val profilePic: ImageKitResponse? = null,
    val schoolClass: String? = null,
    val villageId: Long,
    val groupId: Long,
    val primaryContactNo: String? = null,
    val secondaryContactNo: String? = null,
    val fathersName: String? = null,
    val mothersName: String? = null,
)

data class UpdateStudentRequest(
    val pid: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val yearOfBirth: Int? = null,
    val gender: String? = null,
    val profilePic: ImageKitResponse? = null,
    val schoolClass: String? = null,
    val villageId: Long? = null,
    val groupId: Long? = null,
    val primaryContactNo: String? = null,
    val secondaryContactNo: String? = null,
    val fathersName: String? = null,
    val mothersName: String? = null,
    val isActive: Boolean? = null,
)

data class StudentGroupHistoryResponse(
    val id: Long,
    val oldGroupId: Long?,
    val oldGroupName: String?,
    val newGroupId: Long,
    val newGroupName: String,
    val assignedByPid: String,
    val assignedAt: String
)

data class StudentGroupHistoryListResponse(
    val history: List<StudentGroupHistoryResponse>
)