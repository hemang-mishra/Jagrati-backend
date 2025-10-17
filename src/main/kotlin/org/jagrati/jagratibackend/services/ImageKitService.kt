package org.jagrati.jagratibackend.services

import org.apache.http.HttpHeaders
import org.jagrati.jagratibackend.dto.imagekit.ImageKitResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


private const val IMAGE_KIT_EXPIRY_SECOND = 30 * 60

@Service
class ImageKitService(
    @Value("\${imagekit.private-key}")
    private val privateKey: String
) {
    private val baseUrl = "https://api.imagekit.io/v1"
    private val logger: Logger = LoggerFactory.getLogger(ImageKitService::class.java)

    private val client = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("$privateKey:".toByteArray()))
        .build()

    fun getAuthenticationParameters(): ImageKitResponse {
        val token = UUID.randomUUID().toString()
        val expiry = (System.currentTimeMillis() / 1000) + IMAGE_KIT_EXPIRY_SECOND
        return ImageKitResponse(
            token = token,
            expire = expiry,
            signature = generateSignature(token, expiry)
        )
    }

    fun deleteFile(fileId: String){
        try {
            val response = client.delete()
                .uri("/files/$fileId")
                .retrieve()
                .toBodilessEntity()
                .block()

            if (response?.statusCode?.is2xxSuccessful != true) {
                logger.error("Failed to delete image ${response?.statusCode}")
            }
        }catch (e: Exception){
            logger.error("Failed to delete image ${e.message}")
        }
    }

    private fun generateSignature(token: String, expiry: Long): String {
        val message = token + expiry.toString()
        val mac = Mac.getInstance("HmacSHA1")
        val secretKeySpec = SecretKeySpec(privateKey.toByteArray(), "HmacSHA1")
        mac.init(secretKeySpec)
        val result = mac.doFinal(message.toByteArray())
        return result.toHexString()
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}