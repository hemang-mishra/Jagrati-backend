package org.jagrati.jagratibackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.ZoneOffset
import java.util.TimeZone

@SpringBootApplication
@EnableScheduling
class JagratiBackendApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
    runApplication<JagratiBackendApplication>(*args)
}