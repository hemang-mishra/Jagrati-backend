package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.StringRequest
import org.jagrati.jagratibackend.dto.VillageListResponse
import org.jagrati.jagratibackend.entities.Village
import org.jagrati.jagratibackend.repository.VillageRepository
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
        fcmService.sendSyncNotification()
    }

    fun deleteVillage(request: LongRequest){
        val village = villageRepository.findById(request.value).orElseThrow { IllegalArgumentException("Village not found") }
        villageRepository.save(village.copy(isActive = false))
        fcmService.sendSyncNotification()
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