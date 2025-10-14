package org.jagrati.jagratibackend.services

import org.jagrati.jagratibackend.dto.GroupListResponse
import org.jagrati.jagratibackend.dto.LongRequest
import org.jagrati.jagratibackend.dto.LongStringResponse
import org.jagrati.jagratibackend.dto.NameDescriptionRequest
import org.jagrati.jagratibackend.entities.Group
import org.jagrati.jagratibackend.repository.GroupRepository
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val groupRepository: GroupRepository
) {
    fun addNewGroup(request: NameDescriptionRequest) {
        groupRepository.save(
            Group(
                name = request.name,
                description = request.description
            )
        )
    }

    fun deleteGroup(request: LongRequest) {
        val group = groupRepository.findById(request.value).orElseThrow { IllegalArgumentException("Group not found") }
        groupRepository.save(group.copy(isActive = false))
    }

    fun getAllActiveGroups(): GroupListResponse {
        return GroupListResponse(groupRepository.findAll()
            .filter { it.isActive }
            .map { LongStringResponse(id = it.id, data = it.name) })
    }
}