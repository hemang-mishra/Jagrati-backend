package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.TransitionStatus
import org.jagrati.jagratibackend.entities.enums.TransitionType
import java.time.LocalDateTime

@Entity
@Table(name = "role_transitions")
data class RoleTransition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_pid", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "from_role_id", nullable = false)
    val fromRole: Role,

    @ManyToOne
    @JoinColumn(name = "to_role_id", nullable = false)
    val toRole: Role,

    @Column(name = "transition_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val transitionType: TransitionType,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: TransitionStatus,

    @ManyToOne
    @JoinColumn(name = "requested_by_pid", nullable = false)
    val requestedBy: User,

    @ManyToOne
    @JoinColumn(name = "approved_by_pid")
    val approvedBy: User?,

    @Column(name = "approved_at")
    val approvedAt: LocalDateTime?
) : BaseEntity()