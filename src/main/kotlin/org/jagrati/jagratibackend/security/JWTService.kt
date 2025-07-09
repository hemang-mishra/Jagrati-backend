package org.jagrati.jagratibackend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JWTService(
    @Value("\${jwt.secret}") private val secret: String,
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
//    private val accessTokenValidity = 1000 * 60 * 15L // 15 minutes
    //TODO: Remove this in production
    private val accessTokenValidity = 1000 * 60 * 60 * 24 * 30L * 12//1 yr
    val refreshTokenValidity = 1000 * 60 * 60 * 24 * 30L // 30 days

    private fun generateToken(
        userDetails: UserDetails,
        type: String,
        expiry: Long
    ): String {
        return Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(Date())
            .expiration(Date(Date().time + expiry))
            .claim("type", type)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseToken(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) token.substring(7) else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(userDetails, "access", accessTokenValidity)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, "refresh", refreshTokenValidity)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseToken(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access" && !isTokenExpired(token)
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseToken(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh" && !isTokenExpired(token)
    }

    fun getUserIdFromToken(token: String): String {
        val claims = parseToken(token) ?: throw IllegalArgumentException("Invalid token")
        return claims.subject
    }

    private fun isTokenExpired(token: String): Boolean {
        val claims = parseToken(token) ?: throw IllegalArgumentException("Invalid token")
        return claims.expiration.before(Date())
    }
}