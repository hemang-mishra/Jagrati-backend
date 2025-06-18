package org.jagrati.jagratibackend.security

import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.entities.RefreshTokens
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.repository.RefreshTokenRepository
import org.jagrati.jagratibackend.services.UserService
import org.jagrati.jagratibackend.util.PidGenerator.generatePid
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val jwtService: JWTService,
    private val userService: UserService,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(firstName: String, lastName: String, email: String, password: String): User {
        // Check if email already exists
        val existingUserByEmail = userService.getUserByEmail(email)
        if (existingUserByEmail != null) {
            throw IllegalStateException("User with this email already exists")
        }

        val pid = generatePid(name = firstName)
        // Check if user does not exist with this pid (it won't happen in real world scenarios)
        val user = userService.getUserById(pid)
        if (user != null) {
            throw IllegalStateException("User already registered!")
        }

        // Hash the password
        val hashedPassword = hashEncoder.encode(password)

        // Create new user
        val newUser = User(
            pid = pid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            passwordHash = hashedPassword
        )
        //TODO: Add email verification logic
        // Save to database
        return userService.saveUser(newUser)
    }

    fun login(email: String, password: String): TokenPair {
        val user = userService.getUserByEmail(email) ?: throw BadCredentialsException("Invalid credentials.")
        if (!hashEncoder.matches(password, user.passwordHash)) {
            throw BadCredentialsException("Invalid credentials.")
        }
        val newAccessToken = jwtService.generateAccessToken(user)
        val newRefreshToken = jwtService.generateRefreshToken(user)
        storeRefreshToken(email, newRefreshToken)
        return TokenPair(newAccessToken, newRefreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair{
        if(!jwtService.validateRefreshToken(refreshToken)){ throw IllegalStateException("Invalid refresh token format.") }
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userService.getUserById(userId) ?: throw BadCredentialsException("Invalid refresh token.")
        val hashedRefreshToken = hashToken(refreshToken)
        refreshTokenRepository.findByEmailAndHashedRefreshToken(user.email, hashedRefreshToken)
            ?: throw IllegalArgumentException("Refresh token not recognized (maybe used expired?)")
        refreshTokenRepository.deleteByEmailAndHashedRefreshToken(user.email, hashedRefreshToken)
        val newRefreshToken = jwtService.generateRefreshToken(user)
        val accessToken = jwtService.generateAccessToken(user)
        storeRefreshToken(user.email, newRefreshToken)
        return TokenPair(accessToken, newRefreshToken)
    }

    private fun storeRefreshToken(email: String, rawRefreshToken: String) {
        refreshTokenRepository.save(
            RefreshTokens(
                id = 0,
                email = email,
                hashedRefreshToken = hashToken(rawRefreshToken),
                expiresAt = Instant.now().plusSeconds(jwtService.refreshTokenValidity)
            )
        )
    }

    private fun hashToken(token: String): String{
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}