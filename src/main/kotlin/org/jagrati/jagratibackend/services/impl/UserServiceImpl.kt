package org.jagrati.jagratibackend.services.impl

import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.services.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
): UserService {
    override fun getUserById(pid: String): User? {
        return userRepository.findUserByPid(pid)
    }

    override fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    @Transactional
    override fun saveUser(user: User): User {
        val user = userRepository.save(user)
        val isRoleInitialized = userRoleRepository.findByUser(user).isNotEmpty()
        if(!isRoleInitialized) {
            userRoleRepository.save(
                UserRole(
                    user = user,
                    assignedBy = user,
                    role = roleRepository.findByName("USER") ?: throw IllegalStateException("User role not found")
                )
            )
        }
        return user
    }
}