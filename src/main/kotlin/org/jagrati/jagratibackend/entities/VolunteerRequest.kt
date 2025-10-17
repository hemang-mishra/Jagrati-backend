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
import org.jagrati.jagratibackend.entities.enums.Gender
import org.jagrati.jagratibackend.entities.enums.RequestStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "volunteer_requests")
data class VolunteerRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "roll_number", length = 20)
    val rollNumber: String?,

    @Column(name = "first_name", nullable = false, length = 50)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    val lastName: String,

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(name = "alternate_email", length = 255)
    val alternateEmail: String?,

    @Column(name = "batch", length = 20)
    val batch: String?,

    @Column(name = "programme", length = 100)
    val programme: String?,

    @Column(name = "street_address_1")
    val streetAddress1: String?,

    @Column(name = "street_address_2")
    val streetAddress2: String?,

    @Column(name = "pincode", length = 10)
    val pincode: String?,

    @Column(name = "city", length = 100)
    val city: String?,

    @Column(name = "state", length = 100)
    val state: String?,

    @Column(name = "date_of_birth", nullable = false)
    val dateOfBirth: LocalDate,

    @Column(name = "contact_number", length = 20)
    val contactNumber: String?,

    @Column(name = "college", length = 100)
    val college: String?,

    @Column(name = "branch", length = 100)
    val branch: String?,

    @Column(name = "year_of_study")
    val yearOfStudy: Int?,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: RequestStatus,

    @Column(name = "reason")
    var reason: String? = null,

    @ManyToOne
    @JoinColumn(name = "requested_by_pid", nullable = false)
    val requestedBy: User,

    @ManyToOne
    @JoinColumn(name = "reviewed_by_pid")
    var reviewedBy: User?,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime?
) : BaseEntity()