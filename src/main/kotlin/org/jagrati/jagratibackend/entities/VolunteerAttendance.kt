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
@Table(name = "volunteer_attendance")
data class VolunteerAttendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "volunteer_pid", nullable = false)
    val volunteerPid: Volunteer,

    @ManyToOne
    @JoinColumn(name = "marked_by_user", nullable = false)
    val markedBy: User,

    @Column(name = "attendance_date", nullable = false)
    val attendanceDate: LocalDate,

    @Column(name = "remarks", nullable = true)
    val remarks: String? = null
): BaseEntity()
