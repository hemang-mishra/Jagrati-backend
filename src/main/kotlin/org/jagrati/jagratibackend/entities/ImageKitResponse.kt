package org.jagrati.jagratibackend.entities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


val objectMapper = jacksonObjectMapper()


data class ImageKitResponse(
    val fileId: String = "",
    val name: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val size: Long? = null,
    val filePath: String? = null,
) {
    fun convertToString(): String {
        return objectMapper.writeValueAsString(this)
    }

    companion object {
        fun getFromString(value: String?): ImageKitResponse? {
            if (value == null || value.isEmpty()) {
                return null
            }
            return objectMapper.readValue(value, ImageKitResponse::class.java)
        }
    }
}