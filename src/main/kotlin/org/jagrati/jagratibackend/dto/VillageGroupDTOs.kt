package org.jagrati.jagratibackend.dto

import org.jagrati.jagratibackend.entities.Village
import org.jagrati.jagratibackend.entities.Group

data class VillageDTO(
    val id: Long,
    val name: String,
    val isActive: Boolean
)

data class GroupDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean
)


fun Village.toDTO(): VillageDTO =
    VillageDTO(id = this.id, name = this.name, isActive = this.isActive)

fun VillageDTO.toEntity(): Village =
    Village(id = this.id, name = this.name, isActive = this.isActive)

fun Group.toDTO(): GroupDTO =
    GroupDTO(id = this.id, name = this.name, description = this.description, isActive = this.isActive)

fun GroupDTO.toEntity(): Group =
    Group(id = this.id, name = this.name, description = this.description, isActive = this.isActive)

