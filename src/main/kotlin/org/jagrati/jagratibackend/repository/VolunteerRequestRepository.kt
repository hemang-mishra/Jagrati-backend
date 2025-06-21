package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.VolunteerRequest
import org.jagrati.jagratibackend.entities.enums.RequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VolunteerRequestRepository: JpaRepository<VolunteerRequest, Long> {
    fun findByRollNumber(rollNumber: String): VolunteerRequest?
    fun findByRequestedBy(requestedBy: User): List<VolunteerRequest>
    fun findByStatus(status: RequestStatus): List<VolunteerRequest>
    fun findByStatusAndCreatedAtBetween(status: RequestStatus, startDate: LocalDateTime, endDate: LocalDateTime): List<VolunteerRequest>
    fun findByReviewedBy(reviewedBy: User): List<VolunteerRequest>
    fun existsByRollNumber(rollNumber: String): Boolean
    fun countByStatus(status: RequestStatus): Long
}
