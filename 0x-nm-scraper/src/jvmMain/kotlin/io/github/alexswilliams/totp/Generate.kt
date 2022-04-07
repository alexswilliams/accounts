package io.github.alexswilliams.totp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun generateTOTP(seed: String, steps: Long, codeLength: Int): String =
    hmacSha(seed.asBytes(), steps.hex(16).asBytes())
        .let { it.intAtPosition(it.last4Bits()) }
        .and(0x7fffffff)
        .lastDigits(codeLength)


private fun hmacSha(key: ByteArray, text: ByteArray) =
    Mac.getInstance("HmacSHA1").apply { init(SecretKeySpec(key, "RAW")) }.doFinal(text)

private fun String.asBytes() = this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

private fun ByteArray.intAtPosition(offset: Int) =
    this[offset].toInt() and 0xff shl 24 or
            (this[offset + 1].toInt() and 0xff shl 16) or
            (this[offset + 2].toInt() and 0xff shl 8) or
            (this[offset + 3].toInt() and 0xff)

private fun Long.hex(length: Int) = this.toString(16).uppercase().padStart(length, '0')

private fun ByteArray.last4Bits() = this.last().toInt() and 0x0f

private val POWERS_OF_TEN = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)
private fun Int.lastDigits(length: Int) = this.mod(POWERS_OF_TEN[length]).toString().padStart(length, '0')
