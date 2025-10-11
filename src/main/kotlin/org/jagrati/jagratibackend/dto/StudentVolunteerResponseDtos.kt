package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.Student
import org.jagrati.jagratibackend.entities.Volunteer
import org.jagrati.jagratibackend.entities.Village
import org.jagrati.jagratibackend.entities.Group
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.enums.Gender
import java.time.LocalDate

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


fun Student.toResponse(): StudentResponse = StudentResponse(
    pid = this.pid,
    firstName = this.firstName,
    lastName = this.lastName,
    yearOfBirth = this.yearOfBirth,
    gender = this.gender,
    profilePic = this.profilePic,
    schoolClass = this.schoolClass,
    villageId = this.village.id,
    villageName = this.village.name,
    groupId = this.group.id,
    groupName = this.group.name,
    primaryContactNo = this.primaryContactNo,
    secondaryContactNo = this.secondaryContactNo,
    fathersName = this.fathersName,
    mothersName = this.mothersName,
    isActive = this.isActive
)


fun StudentResponse.toEntity(village: Village, group: Group, registeredBy: User): Student = Student(
    pid = this.pid,
    firstName = this.firstName,
    lastName = this.lastName,
    yearOfBirth = this.yearOfBirth,
    gender = this.gender,
    profilePic = this.profilePic,
    schoolClass = this.schoolClass,
    village = village,
    group = group,
    primaryContactNo = this.primaryContactNo,
    secondaryContactNo = this.secondaryContactNo,
    fathersName = this.fathersName,
    mothersName = this.mothersName,
    isActive = this.isActive,
    registeredBy = registeredBy
)


fun Volunteer.toResponse(): VolunteerResponse = VolunteerResponse(
    pid = this.pid,
    rollNumber = this.rollNumber,
    firstName = this.firstName,
    lastName = this.lastName,
    gender = this.gender,
    alternateEmail = this.alternateEmail,
    batch = this.batch,
    programme = this.programme,
    streetAddress1 = this.streetAddress1,
    streetAddress2 = this.streetAddress2,
    pincode = this.pincode,
    city = this.city,
    state = this.state,
    dateOfBirth = this.dateOfBirth.toString(), // ISO format
    contactNumber = this.contactNumber,
    college = this.college,
    branch = this.branch,
    yearOfStudy = this.yearOfStudy,
    isActive = this.isActive
)

fun VolunteerResponse.toEntity(): Volunteer = Volunteer(
    pid = this.pid,
    rollNumber = this.rollNumber,
    firstName = this.firstName,
    lastName = this.lastName,
    gender = this.gender,
    alternateEmail = this.alternateEmail,
    batch = this.batch,
    programme = this.programme,
    streetAddress1 = this.streetAddress1,
    streetAddress2 = this.streetAddress2,
    pincode = this.pincode,
    city = this.city,
    state = this.state,
    dateOfBirth = LocalDate.parse(this.dateOfBirth),
    contactNumber = this.contactNumber,
    college = this.college,
    branch = this.branch,
    yearOfStudy = this.yearOfStudy,
    isActive = this.isActive
)
