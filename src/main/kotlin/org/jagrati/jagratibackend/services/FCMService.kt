package org.jagrati.jagratibackend.services

import com.google.api.core.ApiFuture
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.repository.FCMTokensRepository
import org.jagrati.jagratibackend.utils.NotificationContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.log

@Service
class FCMService(
    private val fcmTokensRepository: FCMTokensRepository
) {
    private val logger = LoggerFactory.getLogger(FCMService::class.java)

    fun sendNotificationToMultipleDevices(users: List<User>, content: NotificationContent){
        sendNotificationToMultipleDevices(users =users, title=content.title, description=content.description)
    }

    fun sendNotificationToMultipleDevices(users: List<User>, title: String, description: String) {
        val tokens = mutableListOf<String>()
        users.forEach { user ->
            tokens.addAll(fcmTokensRepository.findByUser(user).map { it.deviceId })
        }
        if (tokens.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                sendNotificationToMultipleDevices(tokens, title, description)
            }
        }
    }

    fun sendSycNotification(){
        CoroutineScope(Dispatchers.IO).launch {
            sendDataToTopic("realtime-data-sync", mapOf("Sync" to "true"))
        }
    }

    suspend fun sendDataToTopic(topic: String, data: Map<String, String>){
        val msg = Message.builder()
            .setTopic(topic)
            .putAllData(data)
            .build()

        val future = FirebaseMessaging.getInstance().sendAsync(msg)
        val msgId = future.await()
        logger.info("Data sent to topic: {}", topic)
    }



    private suspend fun sendNotificationToMultipleDevices(tokens: List<String>, title: String, message: String) =
        withContext(Dispatchers.IO) {
            if (tokens.isEmpty()) {
                logger.warn("No tokens provided for broadcast.")
                return@withContext
            }

            val multicastMessage = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(mapOf(Pair("title", title), Pair("message", message)))
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
