package io.github.alexswilliams.migrations.exec

import com.fasterxml.jackson.databind.JsonNode
import io.github.alexswilliams.migrations.Migration
import java.io.File
import java.util.*


object V2ImportAccounts : Migration {
    override val description: String
        get() = "Import accounts from google sheets data"

    override fun migrate(props: Properties) {
        val sheetsAccountsFile: String by props

        val sheetsAccounts = Migration.mapper.readTree(File(sheetsAccountsFile))
        val accounts = importAccounts(sheetsAccounts)

        val accountsDataFilePath: String by props
        Migration.mapper.writeValue(File(accountsDataFilePath), accounts.sorted())
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

    @Suppress("UNCHECKED_CAST")
    private fun List<Any?>.sorted() = (this as List<Map<String, Any?>>)
        .map {
            it.plus(
                "cards" to (it["cards"] as List<Map<String, Any?>>).sortedWith(
                    compareBy(
                        { card -> card["startMonth"] as String? },
                        { card -> (card["id"] as UUID) })
                )
            )
        }
        .sortedWith(compareBy({ it["institution"] as String }, { it["alias"] as String }))
}
