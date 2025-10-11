package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Volunteer
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface VolunteerRepository: JpaRepository<Volunteer, String> {
    fun findAllByUpdatedAtAfter(updatedAt: LocalDateTime): List<Volunteer>
}