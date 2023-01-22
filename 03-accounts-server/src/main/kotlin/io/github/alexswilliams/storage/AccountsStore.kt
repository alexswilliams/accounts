package io.github.alexswilliams.storage

import com.fasterxml.jackson.core.type.TypeReference
import io.github.alexswilliams.mapper
import io.github.alexswilliams.model.AccountType
import io.github.alexswilliams.model.CardNetwork
import java.io.File
import java.time.LocalDate
import java.util.*

data class CardOnDisk(
    val id: UUID,
    val cardNumber: String,
    val network: CardNetwork?,
    val idInSheet: String?,
    val comment: String?,
    val startMonth: LocalDate?,
    val expiryMonth: LocalDate?,
    val activatedDate: LocalDate?,
    val deactivatedDate: LocalDate?,
    val seenInUse: List<String>
)

data class AccountOnDisk(
    val id: UUID,
    val accountType: AccountType,
    val alias: String,
    val institution: String,
    val idInSheet: String?,
    val sortCode: String?,
    val accountNumber: String?,
    val primaryCurrency: String,
    val cards: List<CardOnDisk>
)

object AccountsStore {
    private val props = Properties().apply { load(File("03-accounts-server/storage.properties").inputStream()) }
    private val accountsDataFilePath: String by props
    private val accountsDataFile = File(accountsDataFilePath)

    private val listOfAccountsType = object : TypeReference<List<AccountOnDisk>>() {}


    fun loadAccounts(): List<AccountOnDisk> {
        MigrationStatus.validate()
        return mapper.readValue(accountsDataFile, listOfAccountsType).sorted()
    }

    fun storeAccounts(accounts: List<AccountOnDisk>) {
        MigrationStatus.validate()
        mapper.writeValue(
            accountsDataFile,
            accounts.sorted()
        )
    }

    private fun List<AccountOnDisk>.sorted() = this
        .map { it.copy(cards = it.cards.sortedWith(compareBy({ card -> card.startMonth }, { card -> card.id }))) }
        .sortedWith(compareBy({ it.institution }, { it.alias }))
}
