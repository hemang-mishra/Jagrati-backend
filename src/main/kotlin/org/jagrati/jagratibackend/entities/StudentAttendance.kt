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
import java.time.LocalDate

@Entity
@Table(name = "student_attendance")
data class StudentAttendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_pid", nullable = false)
    val studentId: Student,

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(name = "marked_by_user", nullable = false)
    val markedByVolunteer: User,

    @Column(name = "remarks", nullable = true)
    val remarks: String? = null,
): BaseEntity()
