package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Deleted users are retained so that "marked by", "assigned by" and "registered by"
 * references stay resolvable. Anything that lists or authenticates people must use a
 * `...AndDeletedAtIsNull` variant; the plain finders are for resolving a known
 * reference, including a deleted one.
 */
@Repository
interface UserRepository: JpaRepository<User, String> {

    fun findUserByPid(pid: String): User?

    fun findByPid(pid: String): User?

    fun findByEmail(email: String): User?

    fun findByPidAndDeletedAtIsNull(pid: String): User?

    fun findByEmailAndDeletedAtIsNull(email: String): User?

    fun findAllByDeletedAtIsNull(): List<User>
}
