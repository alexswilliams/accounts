package me.alex.application.persistence

import java.util.*

data class DatabaseConfig(
    val host: String,
    val port: Short,
    val database: String,
    val rootUserName: String,
    val rootPassword: String,
    val migrationUserName: String,
    val migrationPassword: String,
) {
    val url get() = "jdbc:postgresql://${host}:${port}/${database}"
    val bareUrl get() = "jdbc:postgresql://${host}:${port}/"
    val migrationUserProps = asProperties("user" to migrationUserName, "password" to migrationPassword)
    val rootUserProps = asProperties("user" to rootUserName, "password" to rootPassword)
}

fun asProperties(vararg entries: Pair<String, String>) = Properties().apply { putAll(entries) }
