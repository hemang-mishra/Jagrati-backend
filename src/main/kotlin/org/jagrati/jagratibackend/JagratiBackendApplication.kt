package org.jagrati.jagratibackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class JagratiBackendApplication

fun main(args: Array<String>) {
    runApplication<JagratiBackendApplication>(*args)
}
