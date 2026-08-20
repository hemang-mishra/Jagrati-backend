package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.DeletionSource
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A person who volunteers, which is not the same thing as an account.
 *
 * [user] is null for a PROVISIONAL record: someone whose attendance was taken by
 * roll number before they ever opened the app. Signing in with the matching college
 * address links the account to this row, and because attendance points at [pid],
 * the history comes with it without moving any rows.
 *
 * [rollNumber] is the identity anchor and is derived from the verified college
 * address rather than typed. It is released on deletion — it is personal data in
 * its own right — which is why uniqueness is enforced by a partial index over
 * living rows rather than a plain unique constraint.
 */
@Entity
@Table(name = "volunteers")
data class Volunteer(
    @Id
    @Column(name = "pid", length = 64)
    val pid: String,

    @Column(name = "roll_number", length = 32)
    val rollNumber: String? = null,

    @Column(name = "roll_number_normalized", length = 32)
    val rollNumberNormalized: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pid")
    val user: User? = null,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: VolunteerStatus = VolunteerStatus.ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_pid")
    val createdBy: User? = null,

    @Column(name = "first_name", nullable = false, length = 50)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    val lastName: String,

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(name = "alternate_email", length = 255)
    val alternateEmail: String? = null,

    @Column(name = "batch", length = 20)
    val batch: String? = null,

    @Column(name = "programme", length = 100)
    val programme: String? = null,

    @Column(name = "street_address_1", columnDefinition = "TEXT")
    val streetAddress1: String? = null,

    @Column(name = "street_address_2", columnDefinition = "TEXT")
    val streetAddress2: String? = null,

    @Column(name = "pincode", length = 10)
    val pincode: String? = null,

    @Column(name = "city", length = 100)
    val city: String? = null,

    @Column(name = "state", length = 100)
    val state: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @Column(name = "contact_number", length = 20)
    val contactNumber: String? = null,

    @Column(name = "college", length = 100)
    val college: String? = null,

    @Column(name = "branch", length = 100)
    val branch: String? = null,

    @Column(name = "year_of_study")
    val yearOfStudy: Int? = null,

    @Column(name = "profile_pic_details", columnDefinition = "TEXT")
    val profilePicDetails: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "deleted_by_pid", length = 64)
    var deletedByPid: String? = null,

    @Column(name = "deletion_source", length = 20)
    @Enumerated(EnumType.STRING)
    var deletionSource: DeletionSource? = null
) : BaseEntity() {

    val isDeleted: Boolean get() = deletedAt != null

    val isClaimed: Boolean get() = user != null
}
