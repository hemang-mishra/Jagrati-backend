package org.jagrati.jagratibackend.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_roles")
data class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_pid", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,

    @ManyToOne
    @JoinColumn(name = "assigned_by_pid", nullable = false)
    val assignedBy: User
) : BaseEntity()
