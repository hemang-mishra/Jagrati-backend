package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Village
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VillageRepository: JpaRepository<Village, Long> {
    fun findByName(name: String): Village?
    fun findAllByUpdatedAtAfter(updatedAt: LocalDateTime): List<Village>
}