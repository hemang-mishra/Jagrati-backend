package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.DeletionSource

/**
 * Records that a deletion happened and who performed it. Carries no personal data
 * of its own, so it survives the scrub of the account it refers to.
 */
@Entity
@Table(name = "account_deletions")
data class AccountDeletion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "subject_pid", nullable = false, length = 64)
    val subjectPid: String,

    @Column(name = "performed_by_pid", length = 64)
    val performedByPid: String? = null,

    @Column(name = "source", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val source: DeletionSource,

    @Column(name = "had_volunteer_record", nullable = false)
    val hadVolunteerRecord: Boolean
) : BaseEntity()
