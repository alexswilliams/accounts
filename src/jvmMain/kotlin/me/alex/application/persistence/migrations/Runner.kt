package me.alex.application.persistence.migrations

import io.klogging.NoCoLogging
import me.alex.application.persistence.DatabaseConfig
import me.alex.application.persistence.executeQuery
import me.alex.application.persistence.executeUpdate
import java.sql.Connection
import java.sql.DriverManager

abstract class Migration(val description: String) {
    abstract fun migrate(conn: Connection)
}

object Runner : NoCoLogging {
    private val allMigrations: List<Migration> = listOf(
        Init,
        V1InitialTableLayout,
    )

    fun runMigrations(config: DatabaseConfig) {
        val maxVersion =
            DriverManager.getConnection(config.url, config.migrationUserProps).use { conn ->
                conn.executeQuery("SELECT max(migration_version) as max_version FROM migrations.schema_versions") { rs ->
                    if (rs.next()) {
                        rs.getInt("max_version")
                    } else {
                        throw Exception("Database not correctly initialised")
                    }
                }
            }

        if (maxVersion == allMigrations.size - 1) {
            logger.info("Current schema version is $maxVersion, schema is up to date.")
            return
        }
        if (maxVersion >= allMigrations.size) {
            logger.warn("Current schema version ($maxVersion) is greater than any known schemas - aborting")
            throw Exception("Current schema version ($maxVersion) is greater than any known schemas")
        }

        allMigrations.onEachIndexed { index, migration ->
            if (index <= maxVersion) return@onEachIndexed
            logger.info("Running migration $index")
            DriverManager.getConnection(config.url, config.migrationUserProps).use { conn ->
                migration.migrate(conn)
                conn.executeUpdate("INSERT INTO migrations.schema_versions(migration_version, description) VALUES (?, ?)") {
                    it.setInt(1, index)
                    it.setString(2, migration.description)
                }
            }
            logger.info("Migration $index complete")
        }
    }
}
