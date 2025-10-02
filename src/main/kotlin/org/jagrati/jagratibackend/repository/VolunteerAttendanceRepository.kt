package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.VolunteerAttendance
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface VolunteerAttendanceRepository: JpaRepository<VolunteerAttendance, Long> {
    fun findByAttendanceDate(date: LocalDate): List<VolunteerAttendance>
    fun findByVolunteerPidPid(pid: String): List<VolunteerAttendance>
    fun existsByVolunteerPidPidAndAttendanceDate(pid: String, date: LocalDate): Boolean
}