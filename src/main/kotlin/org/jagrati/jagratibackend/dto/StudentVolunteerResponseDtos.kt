package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.enums.Gender

data class StudentResponse(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val yearOfBirth: Int?,
    val gender: Gender,
    val profilePic: String?,
    val schoolClass: String?,
    val villageId: Long,
    val villageName: String,
    val groupId: Long,
    val groupName: String,
    val primaryContactNo: String?,
    val secondaryContactNo: String?,
    val fathersName: String?,
    val mothersName: String?,
    val isActive: Boolean
)

data class VolunteerResponse(
    val pid: String,
    val rollNumber: String?,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val alternateEmail: String?,
    val batch: String?,
    val programme: String?,
    val streetAddress1: String?,
    val streetAddress2: String?,
    val pincode: String?,
    val city: String?,
    val state: String?,
    val dateOfBirth: String,
    val contactNumber: String?,
    val college: String?,
    val branch: String?,
    val yearOfStudy: Int?,
    val isActive: Boolean
)

