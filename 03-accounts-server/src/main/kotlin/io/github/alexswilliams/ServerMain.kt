package io.github.alexswilliams

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.alexswilliams.plugins.configureHTTP
import io.github.alexswilliams.plugins.configureMonitoring
import io.github.alexswilliams.plugins.configureSerialization
import io.github.alexswilliams.storage.MigrationStatus
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    MigrationStatus.validate()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}

val mapper: ObjectMapper = configureMapper(ObjectMapper())
fun configureMapper(mapper: ObjectMapper): ObjectMapper {
    return mapper.setDefaultPrettyPrinter(
        DefaultPrettyPrinter()
            .withoutSpacesInObjectEntries()
            .withObjectIndenter(DefaultIndenter())
            .withArrayIndenter(DefaultIndenter())
    )
        .enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule.Builder().enable(KotlinFeature.NullToEmptyCollection).build())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
}
