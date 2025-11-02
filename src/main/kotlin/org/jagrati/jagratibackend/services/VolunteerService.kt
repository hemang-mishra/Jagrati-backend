package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.UpdateVolunteerRequest
import org.jagrati.jagratibackend.dto.VolunteerResponse
import org.jagrati.jagratibackend.dto.toResponse
import org.jagrati.jagratibackend.entities.ImageKitResponse
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class VolunteerService(
    private val volunteerRepository: VolunteerRepository,
    private val imageKitService: ImageKitService,
    private val fcmService: FCMService
) {
    fun getAllVolunteers(): List<VolunteerResponse> {
        return volunteerRepository.findAll().map { v -> v.toResponse() }
    }

    fun getVolunteerByPid(pid: String): VolunteerResponse {
        val v = volunteerRepository.findById(pid).orElseThrow { IllegalArgumentException("Volunteer not found") }
        return v.toResponse()
    }

    fun updateVolunteerDetails(pid: String, updateRequest: UpdateVolunteerRequest): VolunteerResponse {
        val existingVolunteer = volunteerRepository.findById(pid)
            .orElseThrow { IllegalArgumentException("Volunteer not found") }


        val existingProfilePic = ImageKitResponse.getFromString(existingVolunteer.profilePicDetails)
        if(updateRequest.profilePic?.fileId != existingProfilePic?.fileId){
            if(existingProfilePic?.fileId != null)
            imageKitService.deleteFile(existingProfilePic.fileId)
        }

        val updatedVolunteer = existingVolunteer.copy(
            rollNumber = updateRequest.rollNumber,
            firstName = updateRequest.firstName ?: existingVolunteer.firstName,
            lastName = updateRequest.lastName ?: existingVolunteer.lastName,
            gender = updateRequest.gender ?: existingVolunteer.gender,
            alternateEmail = updateRequest.alternateEmail,
            batch = updateRequest.batch,
            programme = updateRequest.programme,
            streetAddress1 = updateRequest.streetAddress1,
            streetAddress2 = updateRequest.streetAddress2,
            pincode = updateRequest.pincode,
            city = updateRequest.city,
            state = updateRequest.state ?: existingVolunteer.state,
            dateOfBirth = updateRequest.dateOfBirth?.let { LocalDate.parse(it) } ?: existingVolunteer.dateOfBirth,
            contactNumber = updateRequest.contactNumber ?: existingVolunteer.contactNumber,
            college = updateRequest.college ?: existingVolunteer.college,
            branch = updateRequest.branch,
            yearOfStudy = updateRequest.yearOfStudy,
            profilePicDetails = updateRequest.profilePic?.convertToString()
        )

        val savedVolunteer = volunteerRepository.save(updatedVolunteer)
        fcmService.sendSyncNotification()
        return savedVolunteer.toResponse()
    }
}
