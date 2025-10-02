package org.jagrati.jagratibackend.dto

import jakarta.validation.constraints.NotBlank

data class StringRequest(
    @field:NotBlank val value: String
)

data class LongRequest(
    val value: Long
)

data class NameDescriptionRequest(
    val name: String,
    val description: String
)

data class StringResponse(
    val message: String
)

data class LongStringResponse(
    val data: String,
    val id: Long
)