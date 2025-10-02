package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.StudentAttendance
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface StudentAttendanceRepository: JpaRepository<StudentAttendance, Long> {
    fun findByDate(date: LocalDate): List<StudentAttendance>
    fun findByStudentIdPid(pid: String): List<StudentAttendance>
    fun existsByStudentIdPidAndDate(pid: String, date: LocalDate): Boolean
}