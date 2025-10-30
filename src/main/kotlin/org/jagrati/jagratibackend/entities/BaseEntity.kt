package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    @PreRemove
    fun onSoftDelete() {
        updatedAt = LocalDateTime.now()
    }
}