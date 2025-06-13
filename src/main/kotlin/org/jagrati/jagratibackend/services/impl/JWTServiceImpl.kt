package org.jagrati.jagratibackend.services.impl

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.jagrati.jagratibackend.services.JWTService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Base64
import java.util.Date

@Service
class JWTServiceImpl(
    @Value("\${jwt.secret}") private val secret: String,
) : JWTService {

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    private val accessTokenValidity = 1000 * 60 * 15L // 15 minutes
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
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseToken(token: String): Claims?{
        val rawToken = if(token.startsWith("Bearer ")) token.substring(7) else token
        return try{
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        }catch (e: Exception) {
            null
        }
    }

    fun generateAccessToken(userDetails: UserDetails): String{
        return generateToken(userDetails, "access", accessTokenValidity)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, "refresh", refreshTokenValidity)
    }

    fun validateAccessToken(token: String): Boolean{
        val claims = parseToken(token)?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean{
        val claims = parseToken(token)?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }
}