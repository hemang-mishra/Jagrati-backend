package org.jagrati.jagratibackend.services

import AddressDTO
import ApproveVolunteerRequest
import CreateVolunteerRequest
import DetailedVolunteerRequestListResponse
import DetailedVolunteerRequestResponse
import MyVolunteerRequestListResponse
import MyVolunteerRequestResponse
import RejectVolunteerRequest
import UserSummaryDTO
import VolunteerRequestActionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jagrati.jagratibackend.config.InitialRoles
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.entities.Volunteer
import org.jagrati.jagratibackend.entities.VolunteerRequest
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.entities.enums.RequestStatus
import org.jagrati.jagratibackend.repository.*
import org.jagrati.jagratibackend.utils.NotificationContent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class VolunteerRequestService(
    private val volunteerRequestRepository: VolunteerRequestRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val volunteerRepository: VolunteerRepository,
    private val fcmTokensRepository: FCMTokensRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val fcmService: FCMService
) {
    @Transactional
    fun createVolunteerRequest(request: CreateVolunteerRequest, userPid: String): VolunteerRequestActionResponse {
        val user = userRepository.findUserByPid(userPid) ?: throw IllegalArgumentException("User not found")

        val dateOfBirth = try {
            LocalDate.parse(request.dateOfBirth, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format for date of birth. Use format YYYY-MM-DD")
        }

        val gender = try {
            Gender.valueOf(request.gender.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid gender. Must be one of: MALE, FEMALE, OTHER")
        }

        val volunteerRequest = VolunteerRequest(
            id = 0,
            rollNumber = request.rollNumber,
            firstName = request.firstName,
            lastName = request.lastName,
            gender = gender,
            alternateEmail = request.alternateEmail,
            batch = request.batch,
            programme = request.programme,
            streetAddress1 = request.streetAddress1,
            streetAddress2 = request.streetAddress2,
            pincode = request.pincode,
            city = request.city,
            state = request.state,
            dateOfBirth = dateOfBirth,
            contactNumber = request.contactNumber,
            college = request.college,
            branch = request.branch,
            yearOfStudy = request.yearOfStudy,
            status = RequestStatus.PENDING,
            requestedBy = user,
            reviewedBy = null,
            reviewedAt = null
        )

        val savedRequest = volunteerRequestRepository.save(volunteerRequest)

        //Send notifications to people who can approve the request
        val permission = permissionRepository.findByName(AllPermissions.VOLUNTEER_REQUEST_APPROVE.name)
            ?: throw IllegalStateException("Permission not found")
        val roles = rolePermissionRepository.findByPermission(permission = permission)
        val users = mutableListOf<User>()
        roles.forEach { rolePermission ->
            users.addAll(userRoleRepository.findByRole(rolePermission.role).map { it.user })
        }

        val newVolunteerContent = NotificationContent.getNewVolunteeringRequestContent(
            request.firstName, request.lastName, request.rollNumber
        )
        fcmService.sendNotificationToMultipleDevices(users, newVolunteerContent.first, newVolunteerContent.second)


        return VolunteerRequestActionResponse(
            savedRequest.id,
            savedRequest.status.name,
            "Volunteer request submitted successfully"
        )
    }

    fun getAllVolunteerRequests(): List<VolunteerRequestActionResponse> {
        return volunteerRequestRepository.findAll().map {
            VolunteerRequestActionResponse(
                requestId = it.id,
                status = it.status.name,
                message = it.reason ?: "Unknown error"
            )
        }
    }

    @Transactional
    fun approveVolunteerRequest(
        request: ApproveVolunteerRequest,
        approvedByPid: String
    ): VolunteerRequestActionResponse {
        val volunteerRequest = volunteerRequestRepository.findById(request.requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }
        val approvedBy = userRepository.findUserByPid(approvedByPid) ?: throw IllegalArgumentException("User not found")
        volunteerRequest.status = RequestStatus.APPROVED
        volunteerRequest.reviewedBy = approvedBy
        volunteerRequest.reviewedAt = LocalDateTime.now()
        volunteerRequestRepository.save(volunteerRequest)

        //Add volunteer role to user (if not already added)
        val volunteerRole =
            roleRepository.findByName(InitialRoles.VOLUNTEER.name) ?: throw IllegalArgumentException("Role not found")
        val ur = userRoleRepository.findByUserAndRole(volunteerRequest.requestedBy, volunteerRole)
        if (ur == null) {
            userRoleRepository.save(
                UserRole(
                    user = volunteerRequest.requestedBy,
                    role = volunteerRole,
                    assignedBy = approvedBy
                )
            )
        }

        //Send notification
        val content = NotificationContent.getVolunteeringRequestAcceptedContent(approvedBy.firstName)
        fcmService.sendNotificationToMultipleDevices(
            listOf(volunteerRequest.requestedBy),
            content.first,
            content.second
        )
        fcmService.sendSycNotification()

        //Save volunteer details in the table
        volunteerRepository.save(
            Volunteer(
                pid = volunteerRequest.requestedBy.pid,
                rollNumber = volunteerRequest.rollNumber,
                firstName = volunteerRequest.firstName,
                lastName = volunteerRequest.lastName,
                gender = volunteerRequest.gender,
                alternateEmail = volunteerRequest.alternateEmail,
                batch = volunteerRequest.batch,
                programme = volunteerRequest.programme,
                streetAddress1 = volunteerRequest.streetAddress1,
                streetAddress2 = volunteerRequest.streetAddress2,
                pincode = volunteerRequest.pincode,
                city = volunteerRequest.city,
                state = volunteerRequest.state,
                dateOfBirth = volunteerRequest.dateOfBirth,
                contactNumber = volunteerRequest.contactNumber,
                college = volunteerRequest.college,
                branch = volunteerRequest.branch,
                yearOfStudy = volunteerRequest.yearOfStudy,
                isActive = true
            )
        )

        return VolunteerRequestActionResponse(volunteerRequest.id, volunteerRequest.status.name, "Request approved")
    }

    @Transactional
    fun rejectVolunteerRequest(request: RejectVolunteerRequest, rejectedByPid: String): VolunteerRequestActionResponse {
        val volunteerRequest = volunteerRequestRepository.findById(request.requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }
        val rejectedBy = userRepository.findUserByPid(rejectedByPid) ?: throw IllegalArgumentException("User not found")
        volunteerRequest.status = RequestStatus.REJECTED
        volunteerRequest.reviewedBy = rejectedBy
        volunteerRequest.reviewedAt = LocalDateTime.now()
        volunteerRequest.reason = request.reason
        volunteerRequestRepository.save(volunteerRequest)

        //Send notification
        val content =
            NotificationContent.getVolunteeringRequestRejectedContent(request.reason ?: "Unknown reason")
        fcmService.sendNotificationToMultipleDevices(
            listOf(volunteerRequest.requestedBy),
            content.first,
            content.second
        )

        return VolunteerRequestActionResponse(volunteerRequest.id, volunteerRequest.status.name, "Request rejected")
    }

    fun getMyVolunteerRequests(userPid: String): MyVolunteerRequestListResponse {
        val requests = volunteerRequestRepository.findAll().filter { it.requestedBy.pid == userPid }
        val result = requests.map {
            MyVolunteerRequestResponse(
                id = it.id,
                status = it.status.name,
                createdAt = it.createdAt,
                reviewedAt = it.reviewedAt,
                message = it.reason
            )
        }
        return MyVolunteerRequestListResponse(result)
    }

    fun getDetailedVolunteerRequests(): DetailedVolunteerRequestListResponse {
        val requests = volunteerRequestRepository.findAll()
        val detailedRequests = requests.map { mapToDetailedResponse(it) }
        return DetailedVolunteerRequestListResponse(detailedRequests)
    }

    private fun mapToDetailedResponse(request: VolunteerRequest): DetailedVolunteerRequestResponse {
        val addressDTO = AddressDTO(
            streetAddress1 = request.streetAddress1,
            streetAddress2 = request.streetAddress2,
            pincode = request.pincode,
            city = request.city,
            state = request.state
        )

        val requestedByUser = mapUserToSummary(request.requestedBy)
        val reviewedByUser = request.reviewedBy?.let { mapUserToSummary(it) }

        return DetailedVolunteerRequestResponse(
            id = request.id,
            firstName = request.firstName,
            lastName = request.lastName,
            gender = request.gender.name,
            rollNumber = request.rollNumber,
            alternateEmail = request.alternateEmail,
            batch = request.batch,
            programme = request.programme,
            dateOfBirth = request.dateOfBirth,
            contactNumber = request.contactNumber,
            college = request.college,
            branch = request.branch,
            yearOfStudy = request.yearOfStudy,
            address = addressDTO,
            status = request.status.name,
            createdAt = request.createdAt,
            requestedByUser = requestedByUser,
            reviewedByUser = reviewedByUser,
            reviewedAt = request.reviewedAt
        )
    }

    private fun mapUserToSummary(user: User): UserSummaryDTO {
        return UserSummaryDTO(
            pid = user.pid,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            profileImageUrl = user.profilePictureUrl
        )
    }

}