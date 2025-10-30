package org.jagrati.jagratibackend.services.impl

import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.entities.ImageKitResponse
import org.jagrati.jagratibackend.repository.RefreshTokenRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.jagrati.jagratibackend.repository.VolunteerRequestRepository
import org.jagrati.jagratibackend.services.FCMService
import org.jagrati.jagratibackend.services.ImageKitService
import org.jagrati.jagratibackend.services.UserService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val roleRepository: RoleRepository,
    private val volunteerRepository: VolunteerRepository,
    private val volunteerRequestRepository: VolunteerRequestRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val imageKitService: ImageKitService,
    private val fcmService: FCMService,
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

    @Transactional
    override fun deleteUser(pid: String): StringResponse {
        val user = userRepository.findByPid(pid) ?: throw IllegalArgumentException("User not found")
        val volunteer = volunteerRepository.findById(pid).getOrNull()
        val requests = volunteerRequestRepository.findByRequestedBy(user)
        val refreshTokens = refreshTokenRepository.findAllByEmail(user.email)

        // Delete profile picture from ImageKit if exists in volunteer profile
        if (volunteer != null) {
            //Making isActive false to trigger onUpdate() which is crucial to send sync notification to clients
            volunteerRepository.save(volunteer.copy(isActive = true))
            volunteerRepository.flush()

            val profilePic = volunteer.profilePicDetails?.let { ImageKitResponse.getFromString(it) }
            if (profilePic != null) {
                try {
                    imageKitService.deleteFile(profilePic.fileId)
                } catch (e: Exception) {
                    // Log error but continue with deletion
                    println("Failed to delete profile picture from ImageKit: ${e.message}")
                }
            }
            volunteerRepository.delete(volunteer)
        }

        //Making isActive false to trigger onUpdate() which is crucial to send sync notification to clients
        userRepository.save(user.copy(isActive = false))
        userRepository.flush()
        requests.forEach { volunteerRequestRepository.delete(it) }
        refreshTokens.forEach { refreshTokenRepository.delete(it) }
        
        userRepository.delete(user)

        // Send FCM sync notification
        fcmService.sendSyncNotification()

        return StringResponse("User deleted successfully")
    }
}