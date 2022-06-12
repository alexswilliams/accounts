package me.alex.application

import me.alex.application.persistence.migrations.Runner
import me.alex.application.persistence.migrations.initDatabase
import me.alex.application.persistence.migrations.resetDb

fun main() {
    val config = LOCAL_DB_CONFIG

    resetDb(config)
    initDatabase(config)
    Runner.runMigrations(config)
}

