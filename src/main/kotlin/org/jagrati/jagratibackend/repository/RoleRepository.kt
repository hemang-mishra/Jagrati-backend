package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository: JpaRepository<Role, Long> {
    fun findByName(name: String): Role?
    fun findByNameIn(names: List<String>): List<Role>
    fun existsByName(name: String): Boolean
    fun findByIsActiveTrue(): List<Role>
}
