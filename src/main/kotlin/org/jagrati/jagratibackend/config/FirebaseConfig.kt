package org.jagrati.jagratibackend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct
import java.io.ByteArrayInputStream
import java.util.Base64

@Configuration
class FirebaseConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun initFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            val encodedJson = System.getenv("FIREBASE_CREDENTIALS_BASE64")
                ?: throw IllegalStateException("FIREBASE_CREDENTIALS_JSON env var not found")
            val decodedJson = Base64.getDecoder().decode(encodedJson)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(decodedJson)))
                .build()

            FirebaseApp.initializeApp(options)
            logger.info("Firebase initialized.")
        }
    }
}