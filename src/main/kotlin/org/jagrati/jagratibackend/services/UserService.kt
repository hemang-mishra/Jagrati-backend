package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.User

interface UserService {
    fun getUserById(pid: String): User?
    fun getUserByEmail(email: String): User?
    fun saveUser(user: User): User
    fun deleteUser(pid: String): StringResponse
}