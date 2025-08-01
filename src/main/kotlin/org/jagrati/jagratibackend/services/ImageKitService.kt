package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.imagekit.ImageKitResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class ImageKitService(
    @Value("\${imagekit.private-key}")
    private val privateKey: String
) {
    fun getAuthenticationParameters(): ImageKitResponse{
        val token = UUID.randomUUID().toString()
    private val privateKey: String,
    @Value("\${imagekit.expiry-seconds:1800}")
    private val expirySeconds: Long
) {
    fun getAuthenticationParameters(): ImageKitResponse{
        val token = UUID.randomUUID().toString()
        val expiry = (System.currentTimeMillis() / 1000) + expirySeconds
        return ImageKitResponse(
            token = token,
            expire = expiry,
            signature = generateSignature(token, expiry)
        )
    }

   private fun generateSignature(token: String, expiry: Long): String{
        val message = token+expiry.toString()
        val mac = Mac.getInstance("HmacSHA1")
        val secretKeySpec = SecretKeySpec(privateKey.toByteArray(), "HmacSHA1")
        mac.init(secretKeySpec)
        val result = mac.doFinal(message.toByteArray())
        return result.toHexString()
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}