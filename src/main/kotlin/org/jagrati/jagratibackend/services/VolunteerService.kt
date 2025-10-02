package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.VolunteerResponse
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.springframework.stereotype.Service

@Service
class VolunteerService(
    private val volunteerRepository: VolunteerRepository
) {
    fun getAllVolunteers(): List<VolunteerResponse> {
        return volunteerRepository.findAll().map { v ->
            VolunteerResponse(
                pid = v.pid,
                rollNumber = v.rollNumber,
                firstName = v.firstName,
                lastName = v.lastName,
                gender = v.gender,
                alternateEmail = v.alternateEmail,
                batch = v.batch,
                programme = v.programme,
                streetAddress1 = v.streetAddress1,
                streetAddress2 = v.streetAddress2,
                pincode = v.pincode,
                city = v.city,
                state = v.state,
                dateOfBirth = v.dateOfBirth.toString(),
                contactNumber = v.contactNumber,
                college = v.college,
                branch = v.branch,
                yearOfStudy = v.yearOfStudy,
                isActive = v.isActive
            )
        }
    }

    fun getVolunteerByPid(pid: String): VolunteerResponse {
        val v = volunteerRepository.findById(pid).orElseThrow { IllegalArgumentException("Volunteer not found") }
        return VolunteerResponse(
            pid = v.pid,
            rollNumber = v.rollNumber,
            firstName = v.firstName,
            lastName = v.lastName,
            gender = v.gender,
            alternateEmail = v.alternateEmail,
            batch = v.batch,
            programme = v.programme,
            streetAddress1 = v.streetAddress1,
            streetAddress2 = v.streetAddress2,
            pincode = v.pincode,
            city = v.city,
            state = v.state,
            dateOfBirth = v.dateOfBirth.toString(),
            contactNumber = v.contactNumber,
            college = v.college,
            branch = v.branch,
            yearOfStudy = v.yearOfStudy,
            isActive = v.isActive
        )
    }
}

