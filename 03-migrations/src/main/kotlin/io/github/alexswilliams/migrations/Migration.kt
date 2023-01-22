package io.github.alexswilliams.migrations

import io.github.alexswilliams.migrations.exec.*
import java.util.*

interface Migration {
    val description: String
    fun migrate(props: Properties)

    companion object {
        val allMigrations = listOf(
            V1FolderStructure,
            V2ImportAccounts,
            V3ImportSheetsData,
        )
    }
}
