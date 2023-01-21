package io.github.alexswilliams.initialmigration

import io.github.alexswilliams.initialmigration.persistence.migrations.Runner
import io.github.alexswilliams.initialmigration.persistence.migrations.initDatabase
import io.github.alexswilliams.initialmigration.persistence.migrations.resetDb

fun main() {
    val config = LOCAL_DB_CONFIG

    resetDb(config)
    initDatabase(config)
    Runner.runMigrations(config)
}

