package io.github.alexswilliams.totp

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class GenerateTest {
    private val seedFromRfc6238 = "3132333435363738393031323334353637383930"
    private val periodFromRfc6238 = 30L
    private val t0FromRfc6238 = 0L
    private val codeLengthFromRfc6238 = 8

    @TestFactory
    fun `TOTP examples from RFC6238 validate`() = listOf(
        59L to "94287082",
        1111111109L to "07081804",
        1111111111L to "14050471",
        1234567890L to "89005924",
        2000000000L to "69279037",
        20000000000L to "65353130",
    ).map { (inputTimestamp, expectedCode) ->
        DynamicTest.dynamicTest("$inputTimestamp generates $expectedCode") {
            val t: Long = (inputTimestamp - t0FromRfc6238) / periodFromRfc6238
            val actualCode = generateTOTP(seedFromRfc6238, t, codeLengthFromRfc6238)

            assertEquals(expectedCode, actualCode)
        }
    }

    @TestFactory
    fun `base32 examples from RFC4648 are decoded`() =
        listOf(
            "MY======" to "f".toByteArray(),
            "MZXQ====" to "fo".toByteArray(),
            "MZXW6===" to "foo".toByteArray(),
            "MZXW6YQ=" to "foob".toByteArray(),
            "MZXW6YTB" to "fooba".toByteArray(),
            "MZXW6YTBOI======" to "foobar".toByteArray(),
            "X52VNFIS" to byteArrayOf(0xbf.toByte(), 0x75, 0x56, 0x95.toByte(), 0x12),
        ).map { (input, expectedOutput) ->
            DynamicTest.dynamicTest("$input decodes as $expectedOutput") {
                assertContentEquals(expectedOutput, base32ToBytes(input))
            }
        }

    @Test
    fun `live test`() {
        println(generateTOTP(base32ToBytes("ME3WKMZTMEZTAZRY"), Instant.now().epochSecond / 30, 6))
    }
}
