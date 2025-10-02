package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.VolunteerAttendance
import org.springframework.data.jpa.repository.JpaRepository

interface VolunteerAttendanceRepository: JpaRepository<VolunteerAttendance, Long> {
}