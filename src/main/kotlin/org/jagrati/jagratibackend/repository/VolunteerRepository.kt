package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.Volunteer
import org.springframework.data.jpa.repository.JpaRepository

interface VolunteerRepository: JpaRepository<Volunteer, String> {
}