package me.alex.application

import me.alex.application.persistence.DatabaseConfig

val LOCAL_DB_CONFIG = DatabaseConfig(
    host = "localhost",
    port = 5432,
    database = "accounts",
    rootUserName = "postgres",
    rootPassword = "test",
    migrationUserName = "migrations_user",
    migrationPassword = "test"
)
