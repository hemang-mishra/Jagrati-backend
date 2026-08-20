package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.AccountDeletion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountDeletionRepository : JpaRepository<AccountDeletion, Long> {
    fun findBySubjectPid(subjectPid: String): List<AccountDeletion>
}
