package io.github.alexswilliams

import io.github.alexswilliams.storage.AccountsStore
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {

    routing {
        get("/accounts") {
            call.respond(AccountsStore.loadAccounts())
        }
        get("/accounts/refresh") {
            AccountsStore.storeAccounts(AccountsStore.loadAccounts())
            call.respond(AccountsStore.loadAccounts())
        }

        // TODO: Front end assets
        static("/static") {
            resources("static")
        }
    }
}
