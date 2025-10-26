package org.jagrati.jagratibackend.repository

import org.jagrati.jagratibackend.entities.FCMTokens
import org.jagrati.jagratibackend.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface FCMTokensRepository: JpaRepository<FCMTokens, Long> {

    fun findByDeviceId(deviceId: String): FCMTokens?

    fun findByUser(user: User): List<FCMTokens>
}