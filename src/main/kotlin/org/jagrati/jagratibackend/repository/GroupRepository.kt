package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface GroupRepository : JpaRepository<Group, Long> {
    fun findAllByUpdatedAtAfter(updatedAt: LocalDateTime): List<Group>
}
