package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Role
import org.jagrati.jagratibackend.entities.RoleTransition
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.enums.TransitionStatus
import org.jagrati.jagratibackend.entities.enums.TransitionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RoleTransitionRepository: JpaRepository<RoleTransition, Long> {
    fun findByUser(user: User): List<RoleTransition>
    fun findByUserPid(userPid: String): List<RoleTransition>
    fun findByFromRole(fromRole: Role): List<RoleTransition>
    fun findByToRole(toRole: Role): List<RoleTransition>
    fun findByStatus(status: TransitionStatus): List<RoleTransition>
    fun findByTransitionType(transitionType: TransitionType): List<RoleTransition>
    fun findByUserAndStatus(user: User, status: TransitionStatus): List<RoleTransition>
    fun findByRequestedBy(requestedBy: User): List<RoleTransition>
    fun findByApprovedBy(approvedBy: User): List<RoleTransition>
    fun findByStatusAndCreatedAtBetween(status: TransitionStatus, startDate: LocalDateTime, endDate: LocalDateTime): List<RoleTransition>
    fun countByStatus(status: TransitionStatus): Long
}
