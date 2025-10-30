package org.jagrati.jagratibackend.services

import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.dto.StringResponse
import org.jagrati.jagratibackend.dto.StudentGroupHistoryListResponse
import org.jagrati.jagratibackend.dto.StudentRequest
import org.jagrati.jagratibackend.dto.UpdateStudentRequest
import org.jagrati.jagratibackend.dto.StudentGroupHistoryResponse
import org.jagrati.jagratibackend.dto.StudentResponse
import org.jagrati.jagratibackend.dto.toResponse
import org.jagrati.jagratibackend.entities.ImageKitResponse
import org.jagrati.jagratibackend.entities.Student
import org.jagrati.jagratibackend.entities.StudentGroupHistory
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.repository.GroupRepository
import org.jagrati.jagratibackend.repository.StudentGroupHistoryRepository
import org.jagrati.jagratibackend.repository.StudentRepository
import org.jagrati.jagratibackend.repository.VillageRepository
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class StudentDetailsService(
    private val studentRepository: StudentRepository,
    private val villageRepository: VillageRepository,
    private val groupRepository: GroupRepository,
    private val studentGroupHistoryRepository: StudentGroupHistoryRepository,
    private val imageKitService: ImageKitService,
    private val fcmService: FCMService
) {
    @Transactional
    fun registerNewStudent(details: StudentRequest) {
        val village = villageRepository.findById(details.villageId).orElseThrow { IllegalArgumentException("Village not found") }
        val group = groupRepository.findById(details.groupId).orElseThrow { IllegalArgumentException("Group not found") }
        val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("No current user")
        studentRepository.save(
            Student(
                pid = details.pid,
                firstName = details.firstName,
                lastName = details.lastName,
                yearOfBirth = details.yearOfBirth,
                gender = Gender.valueOf(details.gender),
                profilePic = details.profilePic?.convertToString(),
                schoolClass = details.schoolClass,
                village = village,
                group = group,
                primaryContactNo = details.primaryContactNo,
                secondaryContactNo = details.secondaryContactNo,
                fathersName = details.fathersName,
                mothersName = details.mothersName,
                isActive = true,
                registeredBy = currentUser,
            )
        )
        studentGroupHistoryRepository.save(
            StudentGroupHistory(
                student = studentRepository.findById(details.pid).orElseThrow(),
                toGroup = group,
                assignedBy = currentUser,
                assignedAt = LocalDateTime.now()
            )
        )
        fcmService.sendSyncNotification()
    }

    @Transactional
    fun updateStudent(details: UpdateStudentRequest) {
        val existing = studentRepository.findById(details.pid).orElseThrow { IllegalArgumentException("Student not found") }
        val existingGroupId = existing.group.id
        val newVillage = details.villageId?.let { villageRepository.findById(it).orElseThrow { IllegalArgumentException("Village not found") } } ?: existing.village
        val newGroup = details.groupId?.let { groupRepository.findById(it).orElseThrow { IllegalArgumentException("Group not found") } } ?: existing.group
        val updated = existing.copy(
            firstName = details.firstName ?: existing.firstName,
            lastName = details.lastName ?: existing.lastName,
            yearOfBirth = details.yearOfBirth ?: existing.yearOfBirth,
            gender = details.gender?.let { Gender.valueOf(it) } ?: existing.gender,
            profilePic = details.profilePic?.convertToString() ?: existing.profilePic,
            schoolClass = details.schoolClass ?: existing.schoolClass,
            village = newVillage,
            group = newGroup,
            primaryContactNo = details.primaryContactNo ?: existing.primaryContactNo,
            secondaryContactNo = details.secondaryContactNo ?: existing.secondaryContactNo,
            fathersName = details.fathersName ?: existing.fathersName,
            mothersName = details.mothersName ?: existing.mothersName,
            isActive = details.isActive ?: existing.isActive,
        )
        studentRepository.save(updated)
        if(details.profilePic?.fileId != ImageKitResponse.getFromString(existing.profilePic)?.fileId && details.profilePic?.fileId != null){
            val existingProfilePic = ImageKitResponse.getFromString(existing.profilePic)
            if(existingProfilePic?.fileId != null){
                imageKitService.deleteFile(existingProfilePic.fileId)
            }
        }
        if (existingGroupId != newGroup.id) {
            val currentUser = SecurityUtils.getCurrentUser() ?: throw IllegalArgumentException("No current user")
            studentGroupHistoryRepository.save(
                StudentGroupHistory(
                    student = updated,
                    toGroup = newGroup,
                    fromGroup = existing.group,
                    assignedBy = currentUser,
                    assignedAt = LocalDateTime.now()
                )
            )
        }
        fcmService.sendSyncNotification()
    }

    fun getGroupTransitions(pid: String): StudentGroupHistoryListResponse {
        val records = studentGroupHistoryRepository.findByStudentPidOrderByAssignedAtDesc(pid)
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return StudentGroupHistoryListResponse(records.map {
            StudentGroupHistoryResponse(
                id = it.id,
                oldGroupId = it.fromGroup?.id,
                oldGroupName = it.fromGroup?.name,
                newGroupId = it.toGroup.id,
                newGroupName = it.toGroup.name,
                assignedByPid = it.assignedBy.pid,
                assignedAt = it.assignedAt.format(fmt)
            )
        })
    }

    @Transactional
    fun deleteStudent(pid: String): StringResponse {
        val student = studentRepository.findById(pid).orElseThrow { IllegalArgumentException("Student not found") }

        //Making isActive false to trigger onUpdate() which is crucial to send sync notification to clients
        studentRepository.save(student.copy(isActive = false))
        studentRepository.flush()

        //Delete profile data
        val profilePic = student.profilePic?.let { ImageKitResponse.getFromString(it) }
        if(profilePic != null) {
            imageKitService.deleteFile(profilePic.fileId)
        }
        studentRepository.delete(student)
        fcmService.sendSyncNotification()
        return StringResponse("Student deleted")
    }

    fun getAllStudents(): List<StudentResponse> {
        return studentRepository.findAll().map { s -> s.toResponse()}
    }

    fun getStudentByPid(pid: String): StudentResponse {
        val s = studentRepository.findById(pid).orElseThrow { IllegalArgumentException("Student not found") }
        return s.toResponse()
    }
}
