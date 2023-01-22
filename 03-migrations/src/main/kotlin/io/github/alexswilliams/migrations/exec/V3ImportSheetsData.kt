package io.github.alexswilliams.migrations.exec

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.alexswilliams.migrations.Migration
import mu.KotlinLogging
import org.apache.commons.compress.archivers.tar.TarFile
import java.io.File
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.io.path.Path


object V3ImportSheetsData : Migration {
    private val logger = KotlinLogging.logger {}
    override val description: String
        get() = "Import transactions from google sheets data"

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
        val sheetsTransactionsTarball: String by props

        val bytes = GZIPInputStream(Files.newInputStream(Path(sheetsTransactionsTarball))).use { it.readAllBytes() }
        val transactions = TarFile(bytes)
        val txnFileList = transactions.entries.filter { it.isFile }
        logger.info("Found ${txnFileList.size} transactions from google sheets")

        val allAccounts = readAccounts(props)
        val allCards = allAccounts.flatMap {
            @Suppress("UNCHECKED_CAST")
            it["cards"] as List<Map<String, String>>
        }

        val parsedTransactions = txnFileList.mapNotNull { tarFileEntry ->
            val contents = transactions.getInputStream(tarFileEntry).readAllBytes().toString(Charsets.UTF_8)
            val txnBody = mapper.readTree(contents)
            mapTransaction(txnBody, tarFileEntry.name) { sheetId, filePath ->
                val card = allCards.find { it["sheetId"] as String == sheetId }
                val account = allAccounts.find { it["sheetId"] as String == sheetId }
                card?.let { UUID.fromString(it["id"] as String) } to
                        (card?.let { UUID.fromString(it["accountId"] as String) }
                            ?: account?.let { UUID.fromString(it["id"] as String) }
                            ?: throw Exception("No account could be found for $filePath with sheet id $sheetId"))
            }
        }
        val transfers = parsedTransactions.filter { it["opposingHashInSheet"] != null }
        val txnWithOpposingIds = parsedTransactions.map { txn ->
            if (txn["opposingHashInSheet"] == null) txn
            else txn.plus("opposingId" to transfers.first { other -> other["hashInSheet"] as String == txn["opposingHashInSheet"] as String }["id"])
        }
        // check they're 1-to-1 matches
        val keyedTransfers = txnWithOpposingIds.filter { it["opposingHashInSheet"] != null }.associateBy { it["id"] }
        keyedTransfers.values.forEach { txn ->
            val otherId = txn["opposingId"]
            val otherTxn = keyedTransfers[otherId]!!
            if (txn["id"] != otherTxn["opposingId"]) logger.error { "Txn.id != other.opposingId (this=${txn["id"]}, other.opposingId=${otherTxn["opposingId"]}" }
            if (txn["opposingId"] != otherTxn["id"]) logger.error { "Txn.opposingId != other.id (this.opposingId=${txn["opposingId"]}, other=${otherTxn["id"]}" }
        }


        val transactionDataDirectory: String by props
        txnWithOpposingIds
            .groupBy { it["accountId"] as UUID }
            .forEach { (account, txns) ->
                val file = Paths.get(transactionDataDirectory, "$account.json").toFile()
                mapper.writeValue(
                    file,
                    txns.sortedBy { (it["transactionDate"] as String) + "T" + (it["transactionTime"] as String) })
            }

        TODO()
    }

    private fun readAccounts(props: Properties): List<Map<String, Any>> {
        val accountsDataFilePath: String by props
        return mapper.readTree(File(accountsDataFilePath))
            .map { acct ->
                mapOf(
                    "id" to acct["id"].asText(),
                    "sheetId" to acct["idInSheet"].asText(),
                    "cards" to acct["cards"]
                        .filterNot { it["idInSheet"].isNull }
                        .map {
                            mapOf(
                                "id" to it["id"].asText(),
                                "accountId" to acct["id"].asText(),
                                "sheetId" to it["idInSheet"].asText()
                            )
                        }
                )
            }
    }

    private fun mapTransaction(
        txnBody: JsonNode,
        filepath: String,
        lookupAccountAndCard: (String, String) -> Pair<UUID?, UUID>
    ): Map<String, Any?>? {
        val description = txnBody["description"]?.dynamoString().orEmpty()
        if ("No Activity This Month" in description) return null

        val sheetIdentifier = txnBody["accountId"]?.dynamoString()
            ?: throw Exception("Transaction $filepath not linked to account")
        val (cardId, accountId) = lookupAccountAndCard(sheetIdentifier, filepath)
        val creditAmount = txnBody["creditAmount"]?.dynamoNumber()
        val debitAmount = txnBody["debitAmount"]?.dynamoNumber()

        val date = txnBody["date"]!!.dynamoString()
        val rowNumber = txnBody["rowNum"]!!.dynamoNumber()?.toInt()
        val opposingHash = // the matching in step 1 wasn't perfect... neither was the source data.
            if (date == "2015-08-04" && rowNumber == 7) null
            else if (date == "2015-08-15" && rowNumber == 136) "Dt0s_t70OkmJ0MmYn9FuVV45ALQ6z7mHtjMxUdK0gHo="
            else txnBody["opposingTransactionId"]?.dynamoString()

        return mapOf(
            "id" to UUID.randomUUID(),
            "accountId" to accountId,
            "cardId" to cardId,
            "amount" to BigDecimal(
                creditAmount
                    ?: debitAmount
                    ?: (-99999999999L).also { logger.warn("Txn has no amount: $filepath") }).movePointLeft(2)
                .toString(),
            "direction" to if (creditAmount != null) "CREDIT" else "DEBIT",
            "currency" to txnBody["currency"]!!.dynamoString(),
            "transactionDate" to date,
            "transactionTime" to txnBody["time"]!!.dynamoString(),
            "accountInSheet" to txnBody["sheetIdentifier"]?.dynamoString(),
            "categoryInSheet" to null,
            "descriptionInSheet" to description,
            "typeCodeInSheet" to txnBody["typeCode"]!!.dynamoString(),
            "typeInSheet" to txnBody["type"]!!.dynamoString(),
            "hashInSheet" to txnBody["id"]!!.dynamoString(),
            "opposingHashInSheet" to opposingHash,
            "runningBalanceHint" to BigDecimal(
                txnBody["runningBalanceHint"]?.dynamoNumber() ?: -999999999L
            ).movePointLeft(2).toString(),
            "rowInSheet" to (rowNumber ?: throw Exception("Txn has no row: $filepath")),
        )
    }

    private fun JsonNode.dynamoString(): String? = this["S"]?.asText()
    private fun JsonNode.dynamoNumber(): Long? = this["N"]?.asText()?.toLong()
}

