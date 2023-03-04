package io.github.alexswilliams

import io.github.alexswilliams.storage.AccountsStore
import io.github.alexswilliams.storage.TransactionsStore
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Application.configureRouting() {

    routing {
        get("/accounts") {
            call.respond(AccountsStore.loadAccounts())
        }
        get("/transactions") {
            call.respond(TransactionsStore.loadTransactions())
        }
        get("/transactions/{accountId}") {
            call.respond(TransactionsStore.loadTransactionsForAccount(UUID.fromString(call.parameters["accountId"])))
        }
        get("/accounts/refresh") {
            AccountsStore.storeAccounts(AccountsStore.loadAccounts())
            call.respond(AccountsStore.loadAccounts())
        }
        get("/transactions/refresh") {
            TransactionsStore.storeTransactions(TransactionsStore.loadTransactions())
            call.respond(TransactionsStore.loadTransactions())
        }

        // TODO: Front end assets
        static("/static") {
            resources("static")
        }
    }
}
