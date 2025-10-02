package org.jagrati.jagratibackend.dto

data class StudentRequest(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val yearOfBirth: Int? = null,
    val gender: String,
    val profilePic: String? = null,
    val schoolClass: String? = null,
    val villageId: Long,
    val groupId: Long,
    val primaryContactNo: String? = null,
    val secondaryContactNo: String? = null,
    val fathersName: String? = null,
    val mothersName: String? = null,
)