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

@Entity
@Table(name = "fcm_tokens")
data class FCMTokens(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pid", nullable = false)
    val user: User,

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    val deviceId: String
): BaseEntity()
