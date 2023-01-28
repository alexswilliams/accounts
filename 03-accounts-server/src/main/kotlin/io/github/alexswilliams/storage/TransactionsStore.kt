package io.github.alexswilliams.storage

import com.fasterxml.jackson.core.type.TypeReference
import io.github.alexswilliams.mapper
import io.ktor.server.plugins.*
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicReference

enum class Direction {
    CREDIT,
    DEBIT,
}

data class TransactionOnDisk(
    val id: UUID,
    val accountId: UUID,
    val opposingId: UUID?,
    val amount: String,
    val currency: String,
    val direction: Direction,
    val transactionInstant: LocalDateTime,
    val descriptionInSheet: String?,
    val typeInSheet: String,
    val typeCodeInSheet: String,
    val runningBalanceHint: String,
    val hashInSheet: String,
    val opposingHashInSheet: String?,
    val rowInSheet: Int,
    val cardId: UUID?,
)

object TransactionsStore {
    private val props = Properties().apply { load(File("03-accounts-server/storage.properties").inputStream()) }
    private val transactionDataDirectory: String by props
    private val transactionsDataDirectoryFile = File(transactionDataDirectory)

    private val listOfTransactionsType = object : TypeReference<List<TransactionOnDisk>>() {}

    private val storeCache = AtomicReference<Map<UUID, List<TransactionOnDisk>>?>(null)

    fun loadTransactions(): Map<UUID, List<TransactionOnDisk>> {
        MigrationStatus.validate()
        return synchronized(storeCache) {
            storeCache.get()
                ?: transactionsDataDirectoryFile.walkTopDown().filter { it.isFile && it.name.endsWith(".json") }
                    .associateBy({ UUID.fromString(it.name.substringBefore('.')) }) {
                        mapper.readValue(it, listOfTransactionsType).sorted()
                    }.also {
                        storeCache.set(it)
                    }
        }
    }

    fun loadTransactionsForAccount(accountId: UUID): List<TransactionOnDisk> {
        return loadTransactions()[accountId] ?: throw NotFoundException();
    }

    private fun List<TransactionOnDisk>.sorted() = this
        .sortedWith(compareBy({ it.transactionInstant }, { it.amount }, { it.direction }))
}
