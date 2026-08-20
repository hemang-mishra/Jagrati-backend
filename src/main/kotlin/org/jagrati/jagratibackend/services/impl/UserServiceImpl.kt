package org.jagrati.jagratibackend.services.impl

import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.entities.enums.DeletionSource
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.services.AccountDeletionService
import org.jagrati.jagratibackend.services.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
    private val accountDeletionService: AccountDeletionService,
) : UserService {

    /** Resolves a known reference, including a deleted one, so "marked by" still renders. */
    override fun getUserById(pid: String): User? = userRepository.findUserByPid(pid)

    override fun getActiveUserById(pid: String): User? = userRepository.findByPidAndDeletedAtIsNull(pid)

    /**
     * Sign-in lookups must never return a deleted account. Deletion scrubs the address
     * to a pid-derived one, so the real address is already free — this is belt and
     * braces, and it is what stops a half-scrubbed row from being logged into.
     */
    override fun getUserByEmail(email: String): User? = userRepository.findByEmailAndDeletedAtIsNull(email)

    @Transactional
    override fun saveUser(user: User): User {
        val saved = userRepository.save(user)
        val isRoleInitialized = userRoleRepository.findByUser(saved).isNotEmpty()
        if (!isRoleInitialized) {
            userRoleRepository.save(
                UserRole(
                    user = saved,
                    assignedBy = saved,
                    role = roleRepository.findByName("USER") ?: throw IllegalStateException("User role not found")
                )
            )
        }
        return saved
    }

    override fun deleteUser(pid: String, performedBy: User?, source: DeletionSource): StringResponse {
        accountDeletionService.deleteAccount(pid, performedBy, source)
        return StringResponse("Account deleted")
    }
}
