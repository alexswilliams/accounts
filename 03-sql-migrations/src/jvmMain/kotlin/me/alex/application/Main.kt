package me.alex.application

import io.klogging.config.DEFAULT_CONSOLE
import io.klogging.config.loggingConfiguration
import me.alex.application.persistence.DatabaseConfig
import me.alex.application.persistence.migrations.Runner
import me.alex.application.persistence.migrations.initDatabase
import me.alex.application.persistence.migrations.resetDb

fun main() {
    loggingConfiguration { DEFAULT_CONSOLE() }
    val config = LOCAL_DB_CONFIG

    resetDb(config)
    initDatabase(config)
    Runner.runMigrations(config)
}

