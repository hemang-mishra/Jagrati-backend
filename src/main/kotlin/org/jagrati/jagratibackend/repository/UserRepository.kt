package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, String> {

}