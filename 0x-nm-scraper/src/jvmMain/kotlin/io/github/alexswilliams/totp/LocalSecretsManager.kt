package io.github.alexswilliams.totp

import java.util.*

object LocalSecretsManager {
    private val secrets: Properties by lazy { Properties().apply { load(SpecificTotp.javaClass.getResourceAsStream("/secrets.properties")) } }
    fun getValue(key: String): String {
        return secrets.getProperty(key) ?: throw Exception("Could not find secret with name '$key'")
    }
}
