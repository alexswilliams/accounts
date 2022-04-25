package me.alex.application.persistence

import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

fun <T> Connection.executeQuery(@Language("SQL") sql: String, body: (rs: ResultSet) -> T): T =
    this.prepareStatement(sql).use { it.executeQuery().use { rs -> body.invoke(rs) } }

fun <T> Connection.executeQuery(
    @Language("SQL") sql: String,
    statementBody: (PreparedStatement) -> Unit,
    body: (rs: ResultSet) -> T
): T =
    this.prepareStatement(sql).use {
        statementBody(it)
        it.executeQuery().use { rs ->
            body.invoke(rs)
        }
    }

fun Connection.execute(@Language("SQL") sql: String, statementBody: (PreparedStatement) -> Unit = {}) =
    this.prepareStatement(sql).use {
        statementBody(it)
        it.execute()
    }

fun Connection.executeUpdate(@Language("SQL") sql: String, statementBody: (PreparedStatement) -> Unit = {}) =
    this.prepareStatement(sql).use {
        statementBody(it)
        it.executeUpdate()
    }
