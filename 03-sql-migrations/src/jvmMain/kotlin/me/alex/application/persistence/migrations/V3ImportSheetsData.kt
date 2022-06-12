package me.alex.application.persistence.migrations

import kotlinx.serialization.json.*
import me.alex.application.persistence.execute
import me.alex.application.persistence.executeQuery
import me.alex.application.persistence.executeUpdate
import mu.KotlinLogging
import org.apache.commons.compress.archivers.tar.TarFile
import java.sql.Connection
import java.util.*

object V3ImportSheetsData : Migration("Import data dumped from sheets via dynamodb") {
    private val logger = KotlinLogging.logger { }

    override fun migrate(conn: Connection) {
        val transactionTar = javaClass.getResourceAsStream("/transactions.tar")?.readAllBytes()
            ?: throw Exception("Could not find Transactions tape archive")
        val transactions = TarFile(transactionTar)
        val txnFiles = transactions.entries.filter { it.isFile }

        logger.info("Files: ${txnFiles.size}")

        conn.execute("BEGIN TRANSACTION")
        txnFiles.forEach { tarFileEntry ->
            val contents = transactions.getInputStream(tarFileEntry).readAllBytes().toString(Charsets.UTF_8)
            val txnBody = Json.parseToJsonElement(contents)
            importTransaction(conn, txnBody.jsonObject, tarFileEntry.name)
        }
        conn.execute("COMMIT")
    }


    private fun importTransaction(conn: Connection, txnBody: JsonObject, filepath: String) {
        val description = txnBody["description"]?.dynamoStringField() ?: "No Description"
        if ("No Activity This Month" in description) return

        val sheetIdentifier = txnBody.jsonObject["accountId"]?.dynamoStringField()
            ?: throw Exception("Transaction $filepath not linked to account")
        val (cardId, accountId) = lookupAccountAndCard(sheetIdentifier, filepath, conn)
        val creditAmount = txnBody["creditAmount"]?.dynamoNumberField()
        val debitAmount = txnBody["debitAmount"]?.dynamoNumberField()

        val date = txnBody["date"]?.dynamoStringField()
        val rowNumber = txnBody["rowNum"]?.dynamoNumberField()?.toInt()
        val opposingHash = // the matching in step 1 wasn't perfect... neither was the source data.
            if (date == "2015-08-04" && rowNumber == 7) null
            else if (date == "2015-08-15" && rowNumber == 136) "Dt0s_t70OkmJ0MmYn9FuVV45ALQ6z7mHtjMxUdK0gHo="
            else txnBody["opposingTransactionId"]?.dynamoStringField()

        conn.executeUpdate(
            """
INSERT INTO txn
(transaction_id, account_id, card_id, amount_minor_units, direction, currency, transaction_date, transaction_time,
 account_in_sheet, category_in_sheet, description_in_sheet, type_code_in_sheet, type_in_sheet,
 hash_in_sheet, opposing_hash_in_sheet, running_balance_hint, row_in_sheet)
VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?::transaction_direction, ?::currency_code, ?::date, ?::time,
?, ?, ?, ?, ?,
?, ?, ?, ?)"""
        ) {
            it.setString(1, UUID.randomUUID().toString())
            it.setString(2, accountId)
            it.setString(3, cardId)
            it.setLong(
                4,
                creditAmount ?: debitAmount ?: (-99999999999L).also { logger.warn("Txn has no amount: $filepath") })
            it.setString(5, if (creditAmount != null) "CREDIT" else "DEBIT")
            it.setString(6, txnBody["currency"]?.dynamoStringField())
            it.setString(7, txnBody["date"]?.dynamoStringField())
            it.setString(8, txnBody["time"]?.dynamoStringField())
            it.setString(9, sheetIdentifier)
            it.setString(10, null)
            it.setString(11, description)
            it.setString(12, txnBody["typeCode"]?.dynamoStringField())
            it.setString(13, txnBody["type"]?.dynamoStringField())
            it.setString(14, txnBody["id"]?.dynamoStringField())
            it.setString(15, opposingHash)
            it.setLong(16, txnBody["runningBalanceHint"]?.dynamoNumberField() ?: -999999999L)
            it.setInt(17, rowNumber ?: throw Exception("Txn has no row: $filepath"))
        }
    }

    private fun lookupAccountAndCard(identifier: String, filepath: String, conn: Connection): Pair<String?, String> {
        val fromCard = conn.executeQuery(
            """SELECT card_id, account_id FROM card WHERE identifier_in_sheet = ? LIMIT 1""",
            { it.setString(1, identifier) }
        ) { if (it.next()) it.getString("card_id") to it.getString("account_id") else null }

        val fromAccount = conn.executeQuery(
            """SELECT account_id FROM account WHERE identifier_in_sheet = ? LIMIT 1""",
            { it.setString(1, identifier) }
        ) { if (it.next()) it.getString("account_id") else null }

        val cardId = fromCard?.first
        val accountId = fromCard?.second ?: fromAccount ?: throw Exception("No account could be found for $filepath")
        return Pair(cardId, accountId)
    }

    private fun JsonElement.dynamoStringField(): String? = this.jsonObject["S"]?.jsonPrimitive?.content
    private fun JsonElement.dynamoNumberField(): Long? = this.jsonObject["N"]?.jsonPrimitive?.longOrNull
}
