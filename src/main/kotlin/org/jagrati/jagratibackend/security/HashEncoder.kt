package org.jagrati.jagratibackend.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {
    private val bycrpt = BCryptPasswordEncoder()

    fun encode(raw: String): String = bycrpt.encode(raw)

    fun matches(raw: String, hashed:String) = bycrpt.matches(raw, hashed)
}