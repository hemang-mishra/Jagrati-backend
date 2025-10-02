package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "face_data")
data class FaceData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "pid", nullable = false, length = 50)
    val pid: String,

    @Column(name = "name", length = 255)
    val name: String? = null,

    @Column(name = "face_link", length = 255)
    val faceLink: String? = null,

    @Column(name = "frame_link", length = 255)
    val frameLink: String? = null,

    @Column(name = "image_link", length = 255)
    val imageLink: String? = null,

    @Column(name = "width")
    val width: Int? = null,

    @Column(name = "height")
    val height: Int? = null,

    @Column(name = "face_width")
    val faceWidth: Int? = null,

    @Column(name = "face_height")
    val faceHeight: Int? = null,

    @Column(name = "top")
    val top: Int? = null,

    @Column(name = "left")
    val left: Int? = null,

    @Column(name = "right")
    val right: Int? = null,

    @Column(name = "bottom")
    val bottom: Int? = null,

    @Column(name = "landmarks", columnDefinition = "TEXT")
    val landmarks: String? = null,

    @Column(name = "smiling_probability")
    val smilingProbability: Float? = null,

    @Column(name = "left_eye_open_probability")
    val leftEyeOpenProbability: Float? = null,

    @Column(name = "right_eye_open_probability")
    val rightEyeOpenProbability: Float? = null,

    @Column(name = "timestamp", length = 50)
    val timestamp: String? = null,

    @Column(name = "time")
    val time: Long? = null
) : BaseEntity()
