package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.FaceData
import org.springframework.data.jpa.repository.JpaRepository

interface FaceDataRepository: JpaRepository<FaceData, Long> {
}