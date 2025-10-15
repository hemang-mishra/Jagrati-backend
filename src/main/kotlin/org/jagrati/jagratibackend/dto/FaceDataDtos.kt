package org.jagrati.jagratibackend.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jagrati.jagratibackend.entities.FaceData
import org.jagrati.jagratibackend.entities.enums.Gender

data class AddFaceDataRequest(
    val pid: String,
    val name: String? = null,
    val faceResponse: ImageKitResponse?,
    val frameResponse: ImageKitResponse?,
    val imageResponse: ImageKitResponse?,
    val width: Int? = null,
    val height: Int? = null,
    val faceWidth: Int? = null,
    val faceHeight: Int? = null,
    val top: Int? = null,
    val left: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null,
    val landmarks: String? = null,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val timestamp: String? = null,
    val time: Long? = null
)

data class UpdateFaceDataRequest(
    val name: String? = null,
    val faceResponse: ImageKitResponse?,
    val frameResponse: ImageKitResponse?,
    val imageResponse: ImageKitResponse?,
    val width: Int? = null,
    val height: Int? = null,
    val faceWidth: Int? = null,
    val faceHeight: Int? = null,
    val top: Int? = null,
    val left: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null,
    val landmarks: String? = null,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val timestamp: String? = null,
    val time: Long? = null
)

data class FaceDataResponse(
    val id: Long,
    val pid: String,
    val name: String?,
    val faceResponse: ImageKitResponse?,
    val frameResponse: ImageKitResponse?,
    val imageResponse: ImageKitResponse?,
    val width: Int?,
    val height: Int?,
    val faceWidth: Int?,
    val faceHeight: Int?,
    val top: Int?,
    val left: Int?,
    val right: Int?,
    val bottom: Int?,
    val landmarks: String?,
    val smilingProbability: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val timestamp: String?,
    val time: Long?
)

data class StudentWithFaceDataResponse(
    val pid: String,
    val firstName: String,
    val lastName: String,
    val yearOfBirth: Int?,
    val gender: Gender,
    val profilePic: String?,
    val schoolClass: String?,
    val villageId: Long,
    val villageName: String,
    val groupId: Long,
    val groupName: String,
    val primaryContactNo: String?,
    val secondaryContactNo: String?,
    val fathersName: String?,
    val mothersName: String?,
    val isActive: Boolean,
    val faceData: FaceDataResponse
)

data class VolunteerWithFaceDataResponse(
    val pid: String,
    val rollNumber: String?,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val alternateEmail: String?,
    val batch: String?,
    val programme: String?,
    val streetAddress1: String?,
    val streetAddress2: String?,
    val pincode: String?,
    val city: String?,
    val state: String?,
    val dateOfBirth: String,
    val contactNumber: String?,
    val college: String?,
    val branch: String?,
    val yearOfStudy: Int?,
    val isActive: Boolean,
    val faceData: FaceDataResponse
)

val objectMapper = jacksonObjectMapper()

fun FaceData.toResponse(): FaceDataResponse = FaceDataResponse(
    id = this.id,
    pid = this.pid,
    name = this.name,
    faceResponse = objectMapper.readValue(this.faceResponse, ImageKitResponse::class.java),
    frameResponse = objectMapper.readValue(this.frameResponse, ImageKitResponse::class.java),
    imageResponse = objectMapper.readValue(this.faceResponse, ImageKitResponse::class.java),
    width = this.width,
    height = this.height,
    faceWidth = this.faceWidth,
    faceHeight = this.faceHeight,
    top = this.top,
    left = this.left,
    right = this.right,
    bottom = this.bottom,
    landmarks = this.landmarks,
    smilingProbability = this.smilingProbability,
    leftEyeOpenProbability = this.leftEyeOpenProbability,
    rightEyeOpenProbability = this.rightEyeOpenProbability,
    timestamp = this.timestamp,
    time = this.time
)

fun FaceDataResponse.toEntity(): FaceData = FaceData(
    id = this.id,
    pid = this.pid,
    name = this.name,
    faceResponse = objectMapper.writeValueAsString(this.faceResponse),
    frameResponse = objectMapper.writeValueAsString(this.frameResponse),
    imageResponse = objectMapper.writeValueAsString(this.imageResponse),
    width = this.width,
    height = this.height,
    faceWidth = this.faceWidth,
    faceHeight = this.faceHeight,
    top = this.top,
    left = this.left,
    right = this.right,
    bottom = this.bottom,
    landmarks = this.landmarks,
    smilingProbability = this.smilingProbability,
    leftEyeOpenProbability = this.leftEyeOpenProbability,
    rightEyeOpenProbability = this.rightEyeOpenProbability,
    timestamp = this.timestamp,
    time = this.time
)


data class ImageKitResponse(
    val fileId: String = "",
    val name: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val size: Long? = null,
    val filePath: String? = null,
){
    fun convertToString(): String{
        return objectMapper.writeValueAsString(this)
    }

    fun getFromString(value: String?): ImageKitResponse?{
        if(value == null || value.isEmpty()){return null}
        return objectMapper.readValue(value, ImageKitResponse::class.java)
    }
}
