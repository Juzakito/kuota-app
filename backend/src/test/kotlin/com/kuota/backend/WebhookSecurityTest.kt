package com.kuota.backend

import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebhookSecurityTest {

    private val secret = "kuota-test-webhook-secret-0123456789"
    private val timestamp = 1_760_000_000L

    @Test
    fun `generic hmac accepts a valid signature`() {
        val body = """{"event_id":"evt-1","status":"APPROVED"}""".toByteArray(StandardCharsets.UTF_8)
        val signature = HmacWebhookVerifier.sign(secret, timestamp, body)

        assertTrue(
            HmacWebhookVerifier.verify(
                secret = secret,
                timestampHeader = timestamp.toString(),
                signatureHeader = signature,
                body = body,
                now = Instant.ofEpochSecond(timestamp)
            )
        )
    }

    @Test
    fun `generic hmac rejects tampered bodies secrets and stale timestamps`() {
        val body = """{"event_id":"evt-1"}""".toByteArray(StandardCharsets.UTF_8)
        val signature = HmacWebhookVerifier.sign(secret, timestamp, body)
        val now = Instant.ofEpochSecond(timestamp)

        assertFalse(
            HmacWebhookVerifier.verify(
                secret, timestamp.toString(), signature,
                """{"event_id":"evt-2"}""".toByteArray(StandardCharsets.UTF_8), now = now
            )
        )
        assertFalse(HmacWebhookVerifier.verify("another-secret", timestamp.toString(), signature, body, now = now))
        assertFalse(HmacWebhookVerifier.verify(secret, (timestamp + 301).toString(), signature, body, now = now))
        assertFalse(HmacWebhookVerifier.verify(secret, "not-a-number", signature, body, now = now))
        assertFalse(HmacWebhookVerifier.verify(secret, timestamp.toString(), "v1=deadbeef", body, now = now))
        assertFalse(HmacWebhookVerifier.verify(null, timestamp.toString(), signature, body, now = now))
        assertFalse(HmacWebhookVerifier.verify(secret, null, signature, body, now = now))
    }

    @Test
    fun `mercadopago signature validates the documented manifest`() {
        val dataId = "123456789"
        val requestId = "request-abc"
        val manifest = "id:$dataId;request-id:$requestId;ts:$timestamp;"
        val signature = hmacSha256Hex(secret, manifest.toByteArray(StandardCharsets.UTF_8))
        val verifier = MercadoPagoSignatureVerifier(secret, clock = { Instant.ofEpochSecond(timestamp) })

        assertTrue(verifier.verify("ts=$timestamp,v1=$signature", requestId, dataId))
        assertFalse(verifier.verify("ts=$timestamp,v1=$signature", "other-request", dataId))
        assertFalse(verifier.verify("ts=$timestamp,v1=$signature", requestId, "999"))
        assertFalse(verifier.verify("ts=${timestamp + 301},v1=$signature", requestId, dataId))
        assertFalse(verifier.verify("ts=$timestamp", requestId, dataId))
        assertFalse(verifier.verify(null, requestId, dataId))
    }

    @Test
    fun `mercadopago signature supports notifications without a request id`() {
        val dataId = "987654321"
        val manifest = "id:$dataId;ts:$timestamp;"
        val signature = hmacSha256Hex(secret, manifest.toByteArray(StandardCharsets.UTF_8))
        val verifier = MercadoPagoSignatureVerifier(secret, clock = { Instant.ofEpochSecond(timestamp) })

        assertTrue(verifier.verify("ts=$timestamp, v1=$signature", null, dataId))
        assertFalse(verifier.verify("ts=$timestamp, v1=$signature", "unexpected-request", dataId))
    }
}
