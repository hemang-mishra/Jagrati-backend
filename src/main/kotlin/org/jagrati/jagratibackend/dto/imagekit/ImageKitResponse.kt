package org.jagrati.jagratibackend.dto.imagekit

data class ImageKitResponse(
    val token: String,
    val expire: Long,
    val signature: String
)