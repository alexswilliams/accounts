package me.alex.application.persistence.migrations

import me.alex.application.persistence.execute
import java.sql.Connection

object V1InitialTableLayout : Migration("Initial Table Layout") {
    override fun migrate(conn: Connection) {
        conn.execute(
            javaClass.getResource("/migrations/V1-InitialTableLayout.sql")?.readText(Charsets.UTF_8)
                ?: throw Exception("Could not find resource")
        )
    }
}
