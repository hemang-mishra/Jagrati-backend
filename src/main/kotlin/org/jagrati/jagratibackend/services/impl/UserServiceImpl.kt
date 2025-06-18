package org.jagrati.jagratibackend.services.impl

import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.services.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
): UserService {
    override fun getUserById(pid: String): User? {
        return userRepository.findUserByPid(pid)
    }

    override fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun saveUser(user: User): User {
        return userRepository.save(user)
    }
}