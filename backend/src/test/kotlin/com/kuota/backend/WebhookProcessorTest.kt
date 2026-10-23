package com.kuota.backend

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebhookProcessorTest {

    @Test
    fun `hmac webhook settles once and deduplicates retries`() {
        withAccounting { accounting ->
            val repository = FakeRepository()
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, null)
            val body = """
                {
                  "event_id": "evt-100",
                  "provider": "generic_psp",
                  "payment_id": "pay-1",
                  "provider_payment_id": "psp-9",
                  "status": "APPROVED",
                  "amount_cents": 18750,
                  "currency": "PEN",
                  "occurred_at": "2026-10-20T12:00:00Z"
                }
            """.trimIndent()

            val first = processor.processHmac(body)
            assertEquals(200, first.httpStatus)
            assertEquals("APPLIED", first.body["code"])
            val update = repository.updates.single()
            assertEquals("pay-1", update.internalPaymentId)
            assertEquals("generic_psp", update.provider)
            assertEquals("psp-9", update.providerPaymentId)
            assertEquals(ProviderPaymentStatus.APPROVED, update.status)
            assertEquals(18_750L, update.amountCents)
            assertEquals("2026-10-20T12:00:00Z", update.occurredAt)

            val second = processor.processHmac(body)
            assertEquals(200, second.httpStatus)
            assertEquals("DUPLICATE", second.body["code"])
            assertEquals(1, repository.updates.size)
        }
    }

    @Test
    fun `hmac webhook rejects malformed payloads before settlement`() {
        withAccounting { accounting ->
            val repository = FakeRepository()
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, null)

            assertEquals(400, processor.processHmac("not-json").httpStatus)
            assertEquals(
                "WEBHOOK_EVENT_ID_INVALID",
                processor.processHmac(
                    """{"event_id":"","payment_id":"pay-1","status":"APPROVED"}"""
                ).body["code"]
            )
            assertEquals(
                "WEBHOOK_STATUS_INVALID",
                processor.processHmac(
                    """{"event_id":"evt-1","payment_id":"pay-1","status":"MAYBE"}"""
                ).body["code"]
            )
            assertEquals(
                "WEBHOOK_AMOUNT_INVALID",
                processor.processHmac(
                    """{"event_id":"evt-2","payment_id":"pay-1","status":"APPROVED","amount_cents":0}"""
                ).body["code"]
            )
            assertEquals(
                "WEBHOOK_TIMESTAMP_INVALID",
                processor.processHmac(
                    """{"event_id":"evt-3","payment_id":"pay-1","status":"APPROVED","occurred_at":"yesterday"}"""
                ).body["code"]
            )
            assertEquals(
                "WEBHOOK_PROVIDER_INVALID",
                processor.processHmac(
                    """{"event_id":"evt-4","provider":"bad provider!","payment_id":"pay-1","status":"APPROVED"}"""
                ).body["code"]
            )
            assertTrue(repository.updates.isEmpty())
        }
    }

    @Test
    fun `mercadopago webhook reads the authoritative payment and verifies the signed id`() = runBlocking {
        withAccounting { accounting ->
            val repository = FakeRepository()
            val lookup = FakeLookup { providerPaymentId -> approvedDetails(providerPaymentId) }
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, lookup)
            val body = """{"id":"evt-mp-1","type":"payment","action":"payment.updated","data":{"id":"12345"}}"""

            val first = processor.processMercadoPago(body, "12345")
            assertEquals(200, first.httpStatus)
            assertEquals("APPLIED", first.body["code"])
            val update = repository.updates.single()
            assertEquals("pay-internal-1", update.internalPaymentId)
            assertEquals("mercadopago", update.provider)
            assertEquals("12345", update.providerPaymentId)
            assertEquals(ProviderPaymentStatus.APPROVED, update.status)
            assertEquals(18_750L, update.amountCents)
            assertEquals(1, lookup.calls)

            val duplicate = processor.processMercadoPago(body, "12345")
            assertEquals("DUPLICATE", duplicate.body["code"])
            assertEquals(1, lookup.calls)

            val mismatch = processor.processMercadoPago(body, "99999")
            assertEquals(400, mismatch.httpStatus)
            assertEquals("WEBHOOK_DATA_ID_MISMATCH", mismatch.body["code"])
        }
    }

    @Test
    fun `mercadopago webhook ignores other notification types`() = runBlocking {
        withAccounting { accounting ->
            val repository = FakeRepository()
            val lookup = FakeLookup { providerPaymentId -> approvedDetails(providerPaymentId) }
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, lookup)
            val body = """{"id":"evt-mp-2","type":"merchant_order","data":{"id":"12345"}}"""

            val outcome = processor.processMercadoPago(body, "12345")
            assertEquals(200, outcome.httpStatus)
            assertEquals("IGNORED", outcome.body["code"])
            assertEquals(0, lookup.calls)
            assertTrue(repository.updates.isEmpty())
        }
    }

    @Test
    fun `mercadopago lookup failures are retryable`() = runBlocking {
        withAccounting { accounting ->
            val repository = FakeRepository()
            var fail = true
            val lookup = FakeLookup { providerPaymentId ->
                if (fail) throw PaymentProviderException("MERCADO_PAGO_PAYMENT_LOOKUP_FAILED", "lookup failed")
                approvedDetails(providerPaymentId)
            }
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, lookup)
            val body = """{"id":"evt-mp-3","type":"payment","data":{"id":"12345"}}"""

            val failed = processor.processMercadoPago(body, "12345")
            assertEquals(502, failed.httpStatus)
            assertTrue(repository.updates.isEmpty())

            fail = false
            val retried = processor.processMercadoPago(body, "12345")
            assertEquals(200, retried.httpStatus)
            assertEquals("APPLIED", retried.body["code"])
            assertEquals(1, repository.updates.size)
            assertEquals(2, lookup.calls)
        }
    }

    @Test
    fun `unknown payments and unsupported statuses are ignored once`() = runBlocking {
        withAccounting { accounting ->
            val repository = FakeRepository().apply { result = SettlementResult.NotFound }
            val lookup = FakeLookup { providerPaymentId -> approvedDetails(providerPaymentId) }
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, lookup)
            val body = """{"id":"evt-mp-4","type":"payment","data":{"id":"12345"}}"""

            val first = processor.processMercadoPago(body, "12345")
            assertEquals(200, first.httpStatus)
            assertEquals("IGNORED", first.body["code"])

            val second = processor.processMercadoPago(body, "12345")
            assertEquals("DUPLICATE", second.body["code"])
            assertEquals(1, lookup.calls)
        }

        withAccounting { accounting ->
            val repository = FakeRepository()
            val lookup = FakeLookup { providerPaymentId ->
                approvedDetails(providerPaymentId).copy(status = "in_mediation")
            }
            val processor = PaymentWebhookProcessor(repository, accounting, LEDGER_ENV_DEMO, lookup)
            val outcome = processor.processMercadoPago(
                """{"id":"evt-mp-5","type":"payment","data":{"id":"777"}}""",
                "777"
            )

            assertEquals(200, outcome.httpStatus)
            assertEquals("IGNORED", outcome.body["code"])
            assertTrue(repository.updates.isEmpty())
            assertEquals(1, lookup.calls)
        }
    }

    private inline fun withAccounting(block: (FileAccountingStore) -> Unit) {
        val directory = Files.createTempDirectory("kuota-webhook-processor")
        FileAccountingStore(
            directory.resolve("ledger.ndjson"),
            directory.resolve("webhooks.ndjson")
        ).use(block)
    }

    private fun approvedDetails(providerPaymentId: String): ProviderPaymentDetails = ProviderPaymentDetails(
        providerPaymentId = providerPaymentId,
        status = "approved",
        statusDetail = "accredited",
        amountCents = 18_750,
        currency = "PEN",
        externalReference = "pay-internal-1",
        metadataPaymentId = "pay-internal-1",
        paymentMethodId = "visa",
        occurredAt = "2026-10-20T12:00:00Z"
    )

    private class FakeRepository : PaymentSettlementRepository {
        val updates = mutableListOf<ProviderSettlement>()
        var result: SettlementResult = SettlementResult.Applied("SUCCEEDED", "MATCHED")

        override fun settle(update: ProviderSettlement): SettlementResult {
            updates += update
            return result
        }
    }

    private class FakeLookup(
        private val handler: suspend (String) -> ProviderPaymentDetails
    ) : MercadoPagoPaymentLookup {
        var calls = 0

        override suspend fun fetchPayment(providerPaymentId: String): ProviderPaymentDetails {
            calls++
            return handler(providerPaymentId)
        }
    }
}
