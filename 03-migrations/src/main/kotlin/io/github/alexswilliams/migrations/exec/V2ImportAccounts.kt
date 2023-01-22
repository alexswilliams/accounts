package io.github.alexswilliams.migrations.exec

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.alexswilliams.migrations.Migration
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.io.path.Path


object V2ImportAccounts : Migration {
    override val description: String
        get() = "Import accounts from google sheets data"

    private val mapper: ObjectMapper by lazy {
        jacksonObjectMapper()
            .setDefaultPrettyPrinter(
                DefaultPrettyPrinter()
                    .withoutSpacesInObjectEntries()
                    .withObjectIndenter(DefaultIndenter())
                    .withArrayIndenter(DefaultIndenter())
            )
            .enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
    }

    override fun migrate(props: Properties) {
        val sheetsAccountsFile: String by props

        val sheetsAccounts = mapper.readTree(File(sheetsAccountsFile))
        val accounts = importAccounts(sheetsAccounts)

        val accountsDataFilePath: String by props
        mapper.writeValue(File(accountsDataFilePath), accounts)
    }

    private fun importAccounts(sheetsAccounts: JsonNode): List<Any> =
        sheetsAccounts.map { acct ->
            mapOf(
                "id" to UUID.randomUUID(),
                "accountType" to acct["account_type"].asText(),
                "alias" to acct["friendly_name"].asText(),
                "institution" to acct["held_with"].asText(),
                "idInSheet" to acct["identifier_in_sheet"]?.asText(),
                "sortCode" to acct["sort_code"]?.asText(),
                "accountNumber" to acct["account_number"]?.asText(),
                "primaryCurrency" to acct["primary_currency"].asText(),
                "cards" to acct["cards"]?.map { card ->
                    mapOf(
                        "id" to UUID.randomUUID(),
                        "cardNumber" to card["card_number"]!!.asText(),
                        "network" to card["network"]?.asText(),
                        "idInSheet" to card["identifier_in_sheet"]?.asText(),
                        "comment" to card["comment"]?.asText(),
                        "startMonth" to card["start_month"]?.asText()?.let { ym -> "$ym-01" },
                        "expiryMonth" to card["expiry_month"]?.asText()?.let { ym -> "$ym-01" },
                        "activatedDate" to card["activated_date"]?.asText(),
                        "deactivatedDate" to card["deactivated_date"]?.asText(),
                        "seenInUse" to card["in_use"]?.map { it.asText() }.orEmpty(),
                    )
                }.orEmpty()
            )
        }
}
