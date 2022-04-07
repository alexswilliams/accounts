package me.alex.application.persistence.migrations

import io.klogging.NoCoLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.alex.application.persistence.execute
import me.alex.application.persistence.executeUpdate
import java.sql.Connection
import java.util.*

object V2ImportGoogleSheetsData : Migration("Import data from google sheets"), NoCoLogging {
    override fun migrate(conn: Connection) {
        val res = "/"
        val resources = (Thread.currentThread().contextClassLoader.getResourceAsStream(res)
            ?: javaClass.getResourceAsStream(res)).use { stream ->
            stream.bufferedReader().use { it.readLines() }
        }
        logger.info("Resources: $resources")

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
                """INSERT INTO accounts
(account_id, account_type, friendly_name, held_with, primary_currency_code,
 identifier_in_sheet, sort_code, account_number)
VALUES (?::uuid, ?::account_type, ?, ?, ?::currency_code, ?, ?, ?)"""
            ) {
                it.setString(1, accountId.toString())
                it.setString(2, acct["account_type"]?.jsonPrimitive?.content)
                it.setString(3, acct["friendly_name"]?.jsonPrimitive?.content)
                it.setString(4, acct["held_with"]?.jsonPrimitive?.content)
                it.setString(5, acct["primary_currency"]?.jsonPrimitive?.content)
                it.setString(6, acct["identifier_in_sheet"]?.jsonPrimitive?.content)
                it.setString(7, acct["sort_code"]?.jsonPrimitive?.content)
                it.setString(8, acct["account_number"]?.jsonPrimitive?.content)
            }
            acct["cards"]?.jsonArray?.map { it.jsonObject }?.forEach { card ->
                conn.executeUpdate(
                    """INSERT INTO cards
(card_id, account_id, card_number, network, identifier_in_sheet,
 start_date, end_date, activated, deactivated)
VALUES (?::uuid, ?::uuid, ?, ?::card_network, ?, ?::date, ?::date, ?::date, ?::date)"""
                ) {
                    it.setString(1, UUID.randomUUID().toString())
                    it.setString(2, accountId.toString())
                    it.setString(3, card["card_number"]?.jsonPrimitive?.content)
                    it.setString(4, card["network"]?.jsonPrimitive?.content)
                    it.setString(5, card["identifier_in_sheet"]?.jsonPrimitive?.content)
                    it.setString(6, card["start_date"]?.jsonPrimitive?.content)
                    it.setString(7, card["end_date"]?.jsonPrimitive?.content)
                    it.setString(8, card["activated"]?.jsonPrimitive?.content)
                    it.setString(9, card["deactivated"]?.jsonPrimitive?.content)
                }
            }
        }
    }
}
