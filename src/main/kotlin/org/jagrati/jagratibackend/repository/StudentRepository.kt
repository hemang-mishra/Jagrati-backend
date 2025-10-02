package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentRepository: JpaRepository<Student, String> {
    fun findByIsActive(isActive: Boolean): List<Student>
}