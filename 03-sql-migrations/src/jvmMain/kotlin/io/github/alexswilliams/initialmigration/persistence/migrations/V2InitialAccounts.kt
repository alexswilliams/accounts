package io.github.alexswilliams.initialmigration.persistence.migrations

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.github.alexswilliams.initialmigration.persistence.execute
import io.github.alexswilliams.initialmigration.persistence.executeUpdate
import java.sql.Connection
import java.util.*

object V2InitialAccounts : Migration("Import initial accounts") {
    override fun migrate(conn: Connection) {
        conn.execute("BEGIN TRANSACTION")
        importAccounts(conn)
        conn.execute("COMMIT")
    }

    private fun importAccounts(conn: Connection) {
        val hardCodedAccounts = Json.parseToJsonElement(
            javaClass.getResource("/initial-account-details.json")?.readText(Charsets.UTF_8)
                ?: throw Exception("Could not find initial account details json file")
        )
        hardCodedAccounts.jsonArray.map { it.jsonObject }.forEach { acct ->
            val accountId = UUID.randomUUID()
            conn.executeUpdate(
                """INSERT INTO account
(account_id, account_type, friendly_name, held_with,
 identifier_in_sheet, sort_code, account_number)
VALUES (?::uuid, ?::account_type, ?, ?, ?, ?, ?)"""
            ) {
                it.setString(1, accountId.toString())
                it.setString(2, acct["account_type"]?.jsonPrimitive?.content)
                it.setString(3, acct["friendly_name"]?.jsonPrimitive?.content)
                it.setString(4, acct["held_with"]?.jsonPrimitive?.content)
                it.setString(5, acct["identifier_in_sheet"]?.jsonPrimitive?.content)
                it.setString(6, acct["sort_code"]?.jsonPrimitive?.content)
                it.setString(7, acct["account_number"]?.jsonPrimitive?.content)
            }
            acct["cards"]?.jsonArray?.map { it.jsonObject }?.forEach { card ->
                conn.executeUpdate(
                    """INSERT INTO card
(card_id, account_id, card_number, network, identifier_in_sheet, card_comment,
 start_month, expiry_month, activated_date, deactivated_date)
VALUES (?::uuid, ?::uuid, ?, ?::card_network, ?, ?, ?::date, ?::date, ?::date, ?::date)"""
                ) {
                    it.setString(1, UUID.randomUUID().toString())
                    it.setString(2, accountId.toString())
                    it.setString(3, card["card_number"]?.jsonPrimitive?.content)
                    it.setString(4, card["network"]?.jsonPrimitive?.content)
                    it.setString(5, card["identifier_in_sheet"]?.jsonPrimitive?.content)
                    it.setString(6, card["comment"]?.jsonPrimitive?.content)
                    it.setString(7, card["start_month"]?.jsonPrimitive?.content?.let { ym -> "$ym-01" })
                    it.setString(8, card["expiry_month"]?.jsonPrimitive?.content?.let { ym -> "$ym-01" })
                    it.setString(9, card["activated_date"]?.jsonPrimitive?.content)
                    it.setString(10, card["deactivated_date"]?.jsonPrimitive?.content)
                }
            }
        }
    }
}
