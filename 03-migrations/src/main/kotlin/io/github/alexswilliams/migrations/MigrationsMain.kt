package io.github.alexswilliams.migrations

import mu.KotlinLogging
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main() {
    val logger = KotlinLogging.logger("main")
    val props = Properties().apply { load(File("03-migrations/migrations.properties").inputStream()) }
    val migrationVersionFilePath: String by props
    val migrationVersionFile = File(migrationVersionFilePath)
    val currentVersion = if (migrationVersionFile.exists()) migrationVersionFile.readLines().first().toInt() else 0
    logger.info { "Currently at schema version $currentVersion" }

    val migrationsToApply = (currentVersion until Migration.allMigrations.size)
    if (migrationsToApply.isEmpty()) {
        logger.info { "Highest possible schema version is ${Migration.allMigrations.size}; no migrations will be run" }
        exitProcess(0)
    }
    logger.info {
        "Will apply the following migrations: \n" + migrationsToApply.joinToString("\n") {
            " > " + (it + 1) + ": " + Migration.allMigrations[it].description
        }
    }

    migrationsToApply.map { Migration.allMigrations[it] }.forEach {
        logger.info { "Applying migration: '${it.description}'..." }
        try {
            it.migrate(props)
        } catch (e: Throwable) {
            logger.error { "Failed to migrate - reset the data repo to its previous state before retrying" }
            throw e
        }
        logger.info { " > Success" }
    }

    migrationVersionFile.writeText((migrationsToApply.last + 1).toString())
    logger.info { "All migrations complete - schema now at version ${(migrationsToApply.last + 1)}" }
    exitProcess(0)
}

