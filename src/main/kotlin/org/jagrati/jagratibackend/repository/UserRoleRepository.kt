package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Role
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository: JpaRepository<UserRole, Long> {
    fun findByUser(user: User): List<UserRole>
    fun findByUserPid(userPid: String): List<UserRole>
    fun findByRole(role: Role): List<UserRole>
    fun findByRoleId(roleId: Long): List<UserRole>
    fun findByUserAndRole(user: User, role: Role): UserRole?
    fun existsByUserAndRole(user: User, role: Role): Boolean
    fun deleteByUserAndRole(user: User, role: Role)
    fun countByRole(role: Role): Long
}
