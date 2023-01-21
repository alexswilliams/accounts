package io.github.alexswilliams.totp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun generateTOTP(seed: String, steps: Long, codeLength: Int): String =
    generateTOTP(seed.asBytes(), steps, codeLength)

fun generateTOTP(seed: ByteArray, steps: Long, codeLength: Int): String =
    hmacSha(seed, steps.hex(16).asBytes())
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

private val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".mapIndexed { index, c -> c to index }.toMap()
internal fun base32ToBytes(base32: String): ByteArray = base32.trimEnd('=').uppercase()
    .map { BASE32_ALPHABET[it] ?: throw Exception("Input is not a base32 string") }
    .chunked(8).flatMap { chunk ->
        val block = List(8) { if (it <= chunk.lastIndex) chunk[it] else 0 }
        listOf( // 00000111 11222223 33334444 45555566 66677777
            ((block[0] and 0b11111 shl 3) or (block[1] and 0b11100 shr 2)),
            ((block[1] and 0b00011 shl 6) or (block[2] and 0b11111 shl 1) or (block[3] and 0b10000 shr 4)),
            ((block[3] and 0b01111 shl 4) or (block[4] and 0b11110 shr 1)),
            ((block[4] and 0b00001 shl 7) or (block[5] and 0b11111 shl 2) or (block[6] and 0b11000 shr 3)),
            ((block[6] and 0b00111 shl 5) or (block[7] and 0b11111))
        ).map { it.toByte() }
    }
    .dropLastWhile { it == (0).toByte() }
    .toByteArray()
