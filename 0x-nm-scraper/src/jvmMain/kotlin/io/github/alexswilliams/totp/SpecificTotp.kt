package io.github.alexswilliams.totp

import java.time.Instant

object SpecificTotp {
    private val seed: String by lazy { LocalSecretsManager.getValue("totp_seed") }
    fun getCode(instant: Instant = Instant.now()) = generateTOTP(this.seed, instant.epochSecond / 30, 6)
}

fun main() {
    while (true) {
        println(SpecificTotp.getCode())
        Thread.sleep(5000)
    }
}
