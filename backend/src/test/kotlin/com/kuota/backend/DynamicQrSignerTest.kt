package com.kuota.backend

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DynamicQrSignerTest {

    private val secret = "q".repeat(QR_SIGNING_MIN_SECRET_LENGTH)
    private val fixedInstant = Instant.parse("2026-10-22T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    @Test
    fun `generated payload verifies and preserves the signed fields`() {
        val service = DynamicQrSigningService(secret, fixedClock)

        val encoded = service.generateSignedQrPayload(
            transactionId = "tx-demo-1",
            supplierId = "supplier-demo-1",
            supplierName = "Proveedor Demo S.A.C.",
            amountCents = 100_000,
            validityMinutes = 5
        )

        val verified = assertNotNull(service.verifyQrPayload(encoded))
        assertEquals("tx-demo-1", verified.transactionId)
        assertEquals("supplier-demo-1", verified.supplierId)
        assertEquals("Proveedor Demo S.A.C.", verified.supplierName)
        assertEquals(100_000, verified.amountCents)
        assertEquals(fixedInstant.toEpochMilli() + 5 * 60_000, verified.expiresAtEpochMillis)
        assertTrue(verified.signature.isNotBlank())
    }

    @Test
    fun `expired payload is rejected`() {
        val generator = DynamicQrSigningService(secret, fixedClock)
        val encoded = generator.generateSignedQrPayload("tx-demo-1", "supplier-1", "Proveedor Demo", 100_000, 1)
        val afterExpiry = DynamicQrSigningService(
            secret,
            Clock.fixed(fixedInstant.plusSeconds(61), ZoneOffset.UTC)
        )

        assertNull(afterExpiry.verifyQrPayload(encoded))
    }

    @Test
    fun `tampered amount is rejected`() {
        val service = DynamicQrSigningService(secret, fixedClock)
        val encoded = service.generateSignedQrPayload("tx-demo-1", "supplier-1", "Proveedor Demo", 100_000)
        val decoded = String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
        val tamperedJson = decoded.replace("\"amount_cents\":100000", "\"amount_cents\":900000")
        val tampered = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(tamperedJson.toByteArray(Charsets.UTF_8))

        assertNull(service.verifyQrPayload(tampered))
    }

    @Test
    fun `payload signed with another secret is rejected`() {
        val original = DynamicQrSigningService(secret, fixedClock)
        val encoded = original.generateSignedQrPayload("tx-demo-1", "supplier-1", "Proveedor Demo", 100_000)
        val attacker = DynamicQrSigningService("x".repeat(QR_SIGNING_MIN_SECRET_LENGTH), fixedClock)

        assertNull(attacker.verifyQrPayload(encoded))
    }

    @Test
    fun `malformed payloads are rejected without throwing`() {
        val service = DynamicQrSigningService(secret, fixedClock)

        assertNull(service.verifyQrPayload(""))
        assertNull(service.verifyQrPayload("not-base64"))
        assertNull(service.verifyQrPayload(Base64.getUrlEncoder().encodeToString("{}".toByteArray())))
        assertNull(service.verifyQrPayload("a".repeat(QR_MAX_PAYLOAD_CHARS + 1)))
    }

    @Test
    fun `short secrets are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            DynamicQrSigningService("short", fixedClock)
        }
    }

    @Test
    fun `invalid generation inputs are rejected`() {
        val service = DynamicQrSigningService(secret, fixedClock)

        assertFailsWith<IllegalArgumentException> {
            service.generateSignedQrPayload("tx", "supplier", "Proveedor", 0)
        }
        assertFailsWith<IllegalArgumentException> {
            service.generateSignedQrPayload("tx", "supplier", "Proveedor", 100_000, 0)
        }
    }
}
