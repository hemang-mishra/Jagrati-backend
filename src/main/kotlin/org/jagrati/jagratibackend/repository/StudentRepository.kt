package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface StudentRepository: JpaRepository<Student, String> {
    fun findByIsActive(isActive: Boolean): List<Student>

    /** Delta sync; includes deleted rows so clients learn about removals. */
    fun findAllByUpdatedAtAfter(updatedAt: LocalDateTime): List<Student>

    fun findAllByDeletedAtIsNull(): List<Student>

    fun findByPidAndDeletedAtIsNull(pid: String): Student?
}