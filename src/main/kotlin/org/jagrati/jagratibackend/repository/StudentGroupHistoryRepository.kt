package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.StudentGroupHistory
import org.springframework.data.jpa.repository.JpaRepository

interface StudentGroupHistoryRepository: JpaRepository<StudentGroupHistory, Long> {
}