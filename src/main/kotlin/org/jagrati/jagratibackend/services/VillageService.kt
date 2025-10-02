package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.NameDescriptionRequest
import org.jagrati.jagratibackend.dto.StringRequest
import org.jagrati.jagratibackend.dto.StudentRequest
import org.jagrati.jagratibackend.entities.Group
import org.jagrati.jagratibackend.entities.Student
import org.jagrati.jagratibackend.entities.Village
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.repository.GroupRepository
import org.jagrati.jagratibackend.repository.StudentRepository
import org.jagrati.jagratibackend.repository.VillageRepository
import org.jagrati.jagratibackend.utils.SecurityUtils
import org.springframework.stereotype.Service

@Service
class VillageService(
    private val villageRepository: VillageRepository,
    private val studentRepository: StudentRepository,
    private val groupRepository: GroupRepository
) {
    fun addNewVillage(request: StringRequest){
        villageRepository.save(
            Village(name = request.value)
        )
    }

    fun deleteVillage(request: LongRequest){
        val village = villageRepository.findById(request.value).orElseThrow { IllegalArgumentException("Village") }
        villageRepository.save(village.copy(isActive = false))
    }

    fun getAllActiveVillages(): List<LongStringResponse>{
        return villageRepository.findAll().filter { it.isActive }.map {
            LongStringResponse(
                id = it.id,
                data = it.name
            )
        }
    }
}