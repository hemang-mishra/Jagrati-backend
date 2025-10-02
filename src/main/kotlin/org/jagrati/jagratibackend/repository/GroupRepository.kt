package org.jagrati.jagratibackend.repository

import jakarta.persistence.Entity
import org.jagrati.jagratibackend.entities.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository: JpaRepository<Group, Long> {
    fun findByIsActive(isActive: Boolean): List<Group>
}