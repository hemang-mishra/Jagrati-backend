package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.enums.DeletionSource

interface UserService {
    /** Resolves a known pid, deleted or not, so retained references still render. */
    fun getUserById(pid: String): User?

    fun getActiveUserById(pid: String): User?

    /** Excludes deleted accounts; this is the sign-in path. */
    fun getUserByEmail(email: String): User?

    fun saveUser(user: User): User

    fun deleteUser(pid: String, performedBy: User?, source: DeletionSource): StringResponse
}
