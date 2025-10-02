package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "student_group_history")
data class StudentGroupHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_pid", nullable = false)
    val student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_group_id", nullable = true)
    val fromGroup: Group? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_group_id", nullable = false)
    val toGroup: Group,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_pid", nullable = false)
    val assignedBy: User,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: LocalDateTime
) : BaseEntity()
