package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Permission
import org.jagrati.jagratibackend.entities.enums.Action
import org.jagrati.jagratibackend.entities.enums.Module
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository: JpaRepository<Permission, Long> {
    fun findByName(name: String): Permission?
    fun findByNameIn(names: List<String>): List<Permission>
    fun findByModule(module: Module): List<Permission>
    fun findByModuleAndAction(module: Module, action: Action): Permission?
    fun existsByName(name: String): Boolean
}
