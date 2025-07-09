package org.jagrati.jagratibackend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Value("\${app.base-url:http://localhost:8080}")
    private lateinit var baseUrl: String

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Jagrati API")
                    .description("API Documentation for Jagrati")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Jagrati Team")
                            .email("contact@example.com")
                    )
            )
            .servers(
                listOf(
                    Server().url(baseUrl).description("Default Server URL")
                )
            )
    }
}