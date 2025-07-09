package org.jagrati.jagratibackend.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "role_permissions")
data class RolePermission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    val permission: Permission
) : BaseEntity()
