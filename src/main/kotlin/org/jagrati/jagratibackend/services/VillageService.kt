package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.NameDescriptionRequest
import org.jagrati.jagratibackend.dto.StringRequest
import org.jagrati.jagratibackend.dto.StudentRequest
import org.jagrati.jagratibackend.dto.VillageListResponse
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
    private val fcmService: FCMService
) {
    fun addNewVillage(request: StringRequest){
        villageRepository.save(
            Village(name = request.value)
        )
        fcmService.sendSycNotification()
    }

    fun deleteVillage(request: LongRequest){
        val village = villageRepository.findById(request.value).orElseThrow { IllegalArgumentException("Village not found") }
        villageRepository.save(village.copy(isActive = false))
        fcmService.sendSycNotification()
    }

    fun getAllActiveVillages(): VillageListResponse {
        return VillageListResponse(villageRepository.findAll().filter { it.isActive }.map {
            LongStringResponse(
                id = it.id,
                data = it.name
            )
        })
    }

    fun getVillageById(id: Long): LongStringResponse {
        val village = villageRepository.findById(id).orElseThrow { IllegalArgumentException("Village not found") }
        return LongStringResponse(data = village.name, id = village.id)
    }
}