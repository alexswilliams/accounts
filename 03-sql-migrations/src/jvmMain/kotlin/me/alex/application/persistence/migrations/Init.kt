package me.alex.application.persistence.migrations

import io.klogging.NoCoLogging
import me.alex.application.persistence.DatabaseConfig
import me.alex.application.persistence.execute
import me.alex.application.persistence.executeUpdate
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object Init : Migration("Database Bootstrap"), NoCoLogging {
    override fun migrate(conn: Connection) {
        throw Exception("Init is special because it does odd things and must not be run as a general migration")
    }
}

fun initDatabase(config: DatabaseConfig) {
    if (config.migrationPassword.isBlank() or config.migrationPassword.toCharArray().none { it.isLetterOrDigit() }) {
        throw Exception("Migration user's password either blank or contains non-alphanumeric characters")
    }

    try {
        DriverManager.getConnection(config.url, config.migrationUserProps).close()
    } catch (e: SQLException) {
        Init.logger.info("Attempting to initialise database")
        DriverManager.getConnection(config.bareUrl, config.rootUserProps).use { conn ->
            conn.execute("CREATE ROLE ${config.migrationUserName} WITH CREATEROLE LOGIN CONNECTION LIMIT 1 PASSWORD '${config.migrationPassword}'")
            conn.execute("CREATE DATABASE ${config.database} TEMPLATE 'template0' ENCODING 'UTF8' LC_COLLATE 'en_GB.UTF-8' LC_CTYPE 'en_GB.UTF-8' OWNER ${config.migrationUserName} CONNECTION LIMIT -1")
        }
        DriverManager.getConnection(config.url, config.rootUserProps).use { conn ->
            conn.execute("CREATE SCHEMA migrations GRANT ALL ON SCHEMA migrations TO ${config.migrationUserName}")
            conn.execute("CREATE TABLE migrations.schema_versions(migration_version integer primary key, description varchar, applied_at timestamp default (now() at time zone 'utc'))")
            conn.execute("GRANT INSERT, SELECT ON migrations.schema_versions TO ${config.migrationUserName}")
            conn.execute("GRANT ALL ON SCHEMA public TO ${config.migrationUserName}")
            conn.executeUpdate("INSERT INTO migrations.schema_versions(migration_version, description) VALUES(0, 'Database Bootstrap')")
        }
        DriverManager.getConnection(config.url, config.migrationUserProps).close()
    }
}

fun resetDb(config: DatabaseConfig) {
    Init.logger.warn("Completely resetting '${config.database}' database!")
    DriverManager.getConnection(config.bareUrl, config.rootUserProps).use { conn ->
        conn.execute("DROP DATABASE IF EXISTS ${config.database}")
        conn.execute("DROP ROLE IF EXISTS ${config.migrationUserName}")
    }
}
