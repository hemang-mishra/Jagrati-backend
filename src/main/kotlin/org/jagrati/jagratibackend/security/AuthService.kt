package org.jagrati.jagratibackend.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.entities.EmailVerificationToken
import org.jagrati.jagratibackend.entities.PasswordResetToken
import org.jagrati.jagratibackend.entities.RefreshTokens
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.repository.EmailVerificationTokenRepository
import org.jagrati.jagratibackend.repository.PasswordResetTokenRepository
import org.jagrati.jagratibackend.repository.RefreshTokenRepository
import org.jagrati.jagratibackend.services.EmailService
import org.jagrati.jagratibackend.services.UserService
import org.jagrati.jagratibackend.util.PidGenerator
import org.jagrati.jagratibackend.util.PidGenerator.generatePid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.Collections
import java.util.UUID
import kotlin.toString

@Service
class AuthService(
    private val jwtService: JWTService,
    private val userService: UserService,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val emailService: EmailService,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private val googleClientId: String
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
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

        val token = UUID.randomUUID().toString()
        val verificationToken = EmailVerificationToken(
            token = token,
            email = email
        )
        emailVerificationTokenRepository.save(verificationToken)

        // Send verification email
        emailService.sendVerificationEmail(email, token, firstName)
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
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw IllegalStateException("Invalid refresh token format.")
        }
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

    @Transactional
    fun verifyEmail(token: String): Boolean {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid verification token.")
        if (verificationToken.expiresAt.isBefore(Instant.now())) {
            emailVerificationTokenRepository.delete(verificationToken)
            throw IllegalArgumentException("Verification token expired.")
        }
        val user = userService.getUserByEmail(verificationToken.email) ?: throw IllegalArgumentException("User not found.")
        user.isEmailVerified = true
        userService.saveUser(user)

        //Cleanup used token
        emailVerificationTokenRepository.delete(verificationToken)
        return true
    }

    @Transactional
    fun resendVerificationEmail(email: String): Boolean {
        val user = userService.getUserByEmail(email)
            ?: throw IllegalArgumentException("User not found")

        if (user.isEmailVerified) {
            throw IllegalStateException("Email already verified")
        }

        // Check if a token already exists
        val existingToken = emailVerificationTokenRepository.findByEmail(email)

        // If token exists, delete it first
        if (existingToken != null) {
            emailVerificationTokenRepository.delete(existingToken)
            // Force flush to ensure the delete is committed before creating a new token
            emailVerificationTokenRepository.flush()
        }

        // Generate new token
        val token = UUID.randomUUID().toString()
        val verificationToken = EmailVerificationToken(
            token = token,
            email = email
        )
        emailVerificationTokenRepository.save(verificationToken)

        // Send verification email
        emailService.sendVerificationEmail(email, token, user.firstName)

        return true
    }

    @Transactional
    fun initiatePasswordReset(email: String): Boolean {
        val user = userService.getUserByEmail(email)
            ?: throw IllegalArgumentException("User not found")

        // Generate a token
        val token = UUID.randomUUID().toString()

        // Delete any existing tokens for this email
        passwordResetTokenRepository.deleteByEmail(email)

        // Save the new token
        val passwordResetToken = PasswordResetToken(
            token = token,
            email = email
        )
        passwordResetTokenRepository.save(passwordResetToken)

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email, token, user.firstName)
            return true
        } catch (e: Exception) {
            logger.error("Failed to send password reset email to $email", e)
            return false
        }
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String): Boolean {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid or expired password reset token")

        if (resetToken.expiresAt.isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken)
            throw IllegalArgumentException("Password reset token has expired")
        }

        val user = userService.getUserByEmail(resetToken.email)
            ?: throw IllegalArgumentException("User not found")

        // Hash the new password
        val hashedPassword = hashEncoder.encode(newPassword)
        user.passwordHash = hashedPassword
        userService.saveUser(user)

        // Delete the token as it's been used
        passwordResetTokenRepository.delete(resetToken)

        return true
    }

    fun validateResetToken(token: String): Boolean {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: return false

        // Check if token is expired
        if (resetToken.expiresAt.isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken)
            return false
        }

        return true
    }

    fun loginWithGoogle(idTokenString: String): TokenPair{
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singleton(googleClientId))
            .build()

        val idToken = verifier.verify(idTokenString) ?: throw BadCredentialsException("Google idToken not found.")

        val payload: GoogleIdToken.Payload = idToken.payload
        val user = processOAuth2User(
            email = payload.email,
            firstName = payload["given_name"] as String,
            lastName = payload["family_name"] as String? ?: "",
            pictureUrl = payload["picture"] as String?,
        )

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        storeRefreshToken(user.email, accessToken)
        return TokenPair(accessToken, refreshToken)
    }

    fun processOAuth2User(email: String, firstName: String, lastName: String, pictureUrl: String? = null): User {
        // Check if user exists
        var user = userService.getUserByEmail(email)

        if (user == null) {
            // Create new user if not exists
            val pid = PidGenerator.generatePid(name = firstName)
            val password = UUID.randomUUID().toString()
            user = User(
                pid = pid,
                firstName = firstName,
                lastName = lastName,
                email = email,
                passwordHash = hashEncoder.encode(password),
                profilePictureUrl = pictureUrl,
                isEmailVerified = true // Google already verified the email
            )
            userService.saveUser(user)
        }else{
            //Check if profile pic is added, if not, add it
            if(pictureUrl != null){
                user = user.copy(profilePictureUrl = pictureUrl)
                userService.saveUser(user)
            }
        }


        return user
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

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}