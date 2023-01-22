package io.github.alexswilliams.migrations.exec

import io.github.alexswilliams.migrations.Migration
import java.nio.file.Files
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.fileStore

object V1FolderStructure : Migration {
    override val description: String
        get() = "Set up folder structure"

    override fun migrate(props: Properties) {
        val transactionDataDirectory: String by props

        Files.deleteIfExists(Path(transactionDataDirectory))
        Files.createDirectories(Path(transactionDataDirectory))
    }
}