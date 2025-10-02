package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.*
import org.jagrati.jagratibackend.entities.FaceData
import org.jagrati.jagratibackend.repository.FaceDataRepository
import org.jagrati.jagratibackend.repository.StudentRepository
import org.jagrati.jagratibackend.repository.VolunteerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FaceDataService(
    private val faceDataRepository: FaceDataRepository,
    private val studentRepository: StudentRepository,
    private val volunteerRepository: VolunteerRepository
) {
    @Transactional
    fun addFaceData(request: AddFaceDataRequest): FaceDataResponse {
        val pid = request.pid
        if (!studentRepository.existsById(pid) && !volunteerRepository.existsById(pid)) {
            throw IllegalArgumentException("PID not found in students or volunteers")
        }
        faceDataRepository.deleteByPid(pid)
        val saved = faceDataRepository.save(
            FaceData(
                pid = pid,
                name = request.name,
                faceLink = request.faceLink,
                frameLink = request.frameLink,
                imageLink = request.imageLink,
                width = request.width,
                height = request.height,
                faceWidth = request.faceWidth,
                faceHeight = request.faceHeight,
                top = request.top,
                left = request.left,
                right = request.right,
                bottom = request.bottom,
                landmarks = request.landmarks,
                smilingProbability = request.smilingProbability,
                leftEyeOpenProbability = request.leftEyeOpenProbability,
                rightEyeOpenProbability = request.rightEyeOpenProbability,
                timestamp = request.timestamp,
                time = request.time
            )
        )
        return saved.toResponse()
    }

    @Transactional
    fun updateFaceData(pid: String, request: UpdateFaceDataRequest): FaceDataResponse {
        val existing = faceDataRepository.findByPid(pid) ?: throw IllegalArgumentException("Face data not found")
        val updated = existing.copy(
            name = request.name ?: existing.name,
            faceLink = request.faceLink ?: existing.faceLink,
            frameLink = request.frameLink ?: existing.frameLink,
            imageLink = request.imageLink ?: existing.imageLink,
            width = request.width ?: existing.width,
            height = request.height ?: existing.height,
            faceWidth = request.faceWidth ?: existing.faceWidth,
            faceHeight = request.faceHeight ?: existing.faceHeight,
            top = request.top ?: existing.top,
            left = request.left ?: existing.left,
            right = request.right ?: existing.right,
            bottom = request.bottom ?: existing.bottom,
            landmarks = request.landmarks ?: existing.landmarks,
            smilingProbability = request.smilingProbability ?: existing.smilingProbability,
            leftEyeOpenProbability = request.leftEyeOpenProbability ?: existing.leftEyeOpenProbability,
            rightEyeOpenProbability = request.rightEyeOpenProbability ?: existing.rightEyeOpenProbability,
            timestamp = request.timestamp ?: existing.timestamp,
            time = request.time ?: existing.time
        )
        return faceDataRepository.save(updated).toResponse()
    }

    @Transactional
    fun deleteFaceData(pid: String) {
        faceDataRepository.deleteByPid(pid)
    }

    @Transactional(readOnly = true)
    fun getFaceDataByPid(pid: String): FaceDataResponse {
        return faceDataRepository.findByPid(pid)?.toResponse() ?: throw IllegalArgumentException("Face data not found")
    }

    @Transactional(readOnly = true)
    fun getAllStudentsWithFaceData(): List<StudentWithFaceDataResponse> {
        val allFace = faceDataRepository.findAll()
        val pids = allFace.map { it.pid }
        val students = studentRepository.findAllById(pids).associateBy { it.pid }
        return allFace.mapNotNull { fd ->
            val s = students[fd.pid] ?: return@mapNotNull null
            StudentWithFaceDataResponse(
                pid = s.pid,
                firstName = s.firstName,
                lastName = s.lastName,
                yearOfBirth = s.yearOfBirth,
                gender = s.gender,
                profilePic = s.profilePic,
                schoolClass = s.schoolClass,
                villageId = s.village.id,
                villageName = s.village.name,
                groupId = s.group.id,
                groupName = s.group.name,
                primaryContactNo = s.primaryContactNo,
                secondaryContactNo = s.secondaryContactNo,
                fathersName = s.fathersName,
                mothersName = s.mothersName,
                isActive = s.isActive,
                faceData = fd.toResponse()
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAllVolunteersWithFaceData(): List<VolunteerWithFaceDataResponse> {
        val allFace = faceDataRepository.findAll()
        val pids = allFace.map { it.pid }
        val volunteers = volunteerRepository.findAllById(pids).associateBy { it.pid }
        return allFace.mapNotNull { fd ->
            val v = volunteers[fd.pid] ?: return@mapNotNull null
            VolunteerWithFaceDataResponse(
                pid = v.pid,
                rollNumber = v.rollNumber,
                firstName = v.firstName,
                lastName = v.lastName,
                gender = v.gender,
                alternateEmail = v.alternateEmail,
                batch = v.batch,
                programme = v.programme,
                streetAddress1 = v.streetAddress1,
                streetAddress2 = v.streetAddress2,
                pincode = v.pincode,
                city = v.city,
                state = v.state,
                dateOfBirth = v.dateOfBirth.toString(),
                contactNumber = v.contactNumber,
                college = v.college,
                branch = v.branch,
                yearOfStudy = v.yearOfStudy,
                isActive = v.isActive,
                faceData = fd.toResponse()
            )
        }
    }

    private fun FaceData.toResponse(): FaceDataResponse = FaceDataResponse(
        id = id,
        pid = pid,
        name = name,
        faceLink = faceLink,
        frameLink = frameLink,
        imageLink = imageLink,
        width = width,
        height = height,
        faceWidth = faceWidth,
        faceHeight = faceHeight,
        top = top,
        left = left,
        right = right,
        bottom = bottom,
        landmarks = landmarks,
        smilingProbability = smilingProbability,
        leftEyeOpenProbability = leftEyeOpenProbability,
        rightEyeOpenProbability = rightEyeOpenProbability,
        timestamp = timestamp,
        time = time
    )
}

