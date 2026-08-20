package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Volunteer
import org.jagrati.jagratibackend.entities.enums.VolunteerStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface VolunteerRepository: JpaRepository<Volunteer, String> {

    /**
     * Delta sync. Deliberately includes deleted rows: this is the only thing that
     * tells a client a record went away, and a client that never hears about the
     * deletion keeps the stale row forever.
     */
    fun findAllByUpdatedAtAfter(updatedAt: LocalDateTime): List<Volunteer>

    fun findAllByDeletedAtIsNull(): List<Volunteer>

    fun findByPidAndDeletedAtIsNull(pid: String): Volunteer?

    /** The identity lookup. Only living volunteers hold a roll number. */
    fun findByRollNumberNormalizedAndDeletedAtIsNull(rollNumberNormalized: String): Volunteer?

    fun existsByRollNumberNormalizedAndDeletedAtIsNull(rollNumberNormalized: String): Boolean

    fun findByUserPidAndDeletedAtIsNull(userPid: String): Volunteer?

    fun existsByUserPidAndDeletedAtIsNull(userPid: String): Boolean

    fun findAllByStatusAndDeletedAtIsNull(status: VolunteerStatus): List<Volunteer>

    @Query("SELECT v FROM Volunteer v WHERE v.deletedAt IS NULL AND v.rollNumberNormalized IS NULL")
    fun findAllMissingRollNumber(): List<Volunteer>

    @Query(
        """
        SELECT v FROM Volunteer v
        WHERE v.deletedAt IS NULL
          AND v.rollNumberNormalized IN :rollNumbers
        """
    )
    fun findAllByRollNumbers(@Param("rollNumbers") rollNumbers: Collection<String>): List<Volunteer>
}
