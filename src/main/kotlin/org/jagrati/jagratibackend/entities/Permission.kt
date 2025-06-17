package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.Action
import org.jagrati.jagratibackend.entities.enums.Module

@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "module", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val module: Module,

    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val action: Action
) : BaseEntity()
