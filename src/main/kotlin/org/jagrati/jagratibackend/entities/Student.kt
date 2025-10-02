package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.jagrati.jagratibackend.entities.enums.Gender

@Entity
@Table(name = "students")
data class Student(
    @Id
    @Column(name = "pid", length = 50)
    val pid: String,

    @Column(name = "first_name", nullable = false, length = 50)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    val lastName: String,

    @Column(name = "year_of_birth")
    val yearOfBirth: Int? = null,

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(name = "profile_pic", length = 255)
    val profilePic: String? = null,

    @Column(name = "school_class", length = 20)
    val schoolClass: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    val village: Village,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @Column(name = "primary_contact_no", length = 20)
    val primaryContactNo: String? = null,

    @Column(name = "secondary_contact_no", length = 20)
    val secondaryContactNo: String? = null,

    @Column(name = "fathers_name", length = 100)
    val fathersName: String? = null,

    @Column(name = "mothers_name", length = 100)
    val mothersName: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "registered_by_pid", nullable = false)
    val registeredBy: User
) : BaseEntity()
