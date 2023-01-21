package io.github.alexswilliams.initialmigration

import io.github.alexswilliams.initialmigration.persistence.DatabaseConfig

val LOCAL_DB_CONFIG = DatabaseConfig(
    host = "localhost",
    port = 5432,
    database = "accounts",
    rootUserName = "postgres",
    rootPassword = "test",
    migrationUserName = "migrations_user",
    migrationPassword = "test"
)
