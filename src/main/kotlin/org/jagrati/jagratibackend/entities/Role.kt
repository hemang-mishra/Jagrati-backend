package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) : BaseEntity()