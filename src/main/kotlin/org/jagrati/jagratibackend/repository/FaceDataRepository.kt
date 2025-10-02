package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.FaceData
import org.springframework.data.jpa.repository.JpaRepository

interface FaceDataRepository: JpaRepository<FaceData, Long> {
    fun findByPid(pid: String): FaceData?
    fun existsByPid(pid: String): Boolean
    fun deleteByPid(pid: String): Long
    fun findAllByPidIn(pids: List<String>): List<FaceData>
}