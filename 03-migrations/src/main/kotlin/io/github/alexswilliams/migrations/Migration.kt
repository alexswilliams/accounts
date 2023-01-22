package io.github.alexswilliams.migrations

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

        val mapper: ObjectMapper by lazy {
            jacksonObjectMapper()
                .setDefaultPrettyPrinter(
                    DefaultPrettyPrinter()
                        .withoutSpacesInObjectEntries()
                        .withObjectIndenter(DefaultIndenter())
                        .withArrayIndenter(DefaultIndenter())
                )
                .enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        }
    }
}
