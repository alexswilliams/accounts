package io.github.alexswilliams.storage

import java.io.File
import java.util.*

object MigrationStatus {
    private val props = Properties().apply { load(File("03-accounts-server/storage.properties").inputStream()) }
    private val expectedVersion: String by props
    private val migrationVersionFilePath: String by props
    private val migrationVersionFile = File(migrationVersionFilePath)

    private fun isFullyMigrated(): Boolean {
        val currentVersion = if (migrationVersionFile.exists()) migrationVersionFile.readLines().first().toInt() else 0
        return currentVersion == expectedVersion.toInt()
    }

    fun validate() {
        if (!isFullyMigrated())
            throw Exception("Migrations are not in sync with server application")
    }
}