package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.StudentAttendance
import org.springframework.data.jpa.repository.JpaRepository

interface StudentAttendanceRepository: JpaRepository<StudentAttendance, Long> {
}