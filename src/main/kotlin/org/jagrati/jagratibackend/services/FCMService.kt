package org.jagrati.jagratibackend.services

import com.google.api.core.ApiFuture
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Service
class FCMService {

    private val logger = LoggerFactory.getLogger(FCMService::class.java)

    suspend fun sendNotificationToDevice(token: String, title: String, message: String): String =
        withContext(Dispatchers.IO) {
            val notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build()

            val msg = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build()

            val future: ApiFuture<String> = FirebaseMessaging.getInstance().sendAsync(msg)
            val messageId = future.await()

            logger.info("Sent FCM message with ID: $messageId")
            messageId
        }


    suspend fun sendNotificationToMultipleDevices(tokens: List<String>, title: String, message: String) =
        withContext(Dispatchers.IO) {
            if (tokens.isEmpty()) {
                logger.warn("No tokens provided for broadcast.")
                return@withContext
            }

            val notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build()

            val multicastMessage = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .build()

            val future = FirebaseMessaging.getInstance().sendEachForMulticastAsync(multicastMessage)
            val response = future.await()

            logger.info("Broadcast complete — Success: ${response.successCount}, Failures: ${response.failureCount}")

            if (response.failureCount > 0) {
                response.responses
                    .filter { !it.isSuccessful }
                    .forEachIndexed { index, res ->
                        logger.warn(" Failed token: ${tokens[index]}, Error: ${res.exception?.message}")
                    }
            }

            response
        }
}

suspend fun <T> ApiFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
    this.addListener(
        {
            try {
                cont.resume(this.get())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        },
        Runnable::run
    )
}
