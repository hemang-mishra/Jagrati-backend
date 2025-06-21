package org.jagrati.jagratibackend.dto

data class MessageResponse(val message: String)
data class GoogleLoginUrlResponse(val url: String)
data class SecuredResponse(
    val message: String,
    val timestamp: String,
    val authenticated: Boolean,
    val username: String,
    val authorities: List<String>
)
data class HelloResponse(
    val message: String,
    val timestamp: String
)
data class RegisterResponse(val pid: String, val firstName: String, val lastName: String, val email: String)

