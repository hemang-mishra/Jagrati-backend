package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.Gender
import java.time.LocalDate

@Entity
@Table(name = "volunteers")
data class Volunteer(
    @Id
    @Column(name = "pid", length = 50)
    val pid: String,

    @Column(name = "roll_number", unique = true, length = 20)
    val rollNumber: String? = null,

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

    @Column(name = "date_of_birth", nullable = false)
    val dateOfBirth: LocalDate,

    @Column(name = "contact_number", length = 20)
    val contactNumber: String? = null,

    @Column(name = "college", length = 100)
    val college: String? = null,

    @Column(name = "branch", length = 100)
    val branch: String? = null,

    @Column(name = "year_of_study")
    val yearOfStudy: Int? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) : BaseEntity()
