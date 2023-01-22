package io.github.alexswilliams.plugins

import io.github.alexswilliams.configureMapper
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            configureMapper(this)
        }
    }
}
