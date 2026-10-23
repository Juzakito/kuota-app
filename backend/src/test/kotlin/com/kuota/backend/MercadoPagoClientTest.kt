package com.kuota.backend

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MercadoPagoClientTest {

    @Test
    fun `creates a checkout preference with idempotency and returns the init point`() = runBlocking {
        var capturedPath: String? = null
        var capturedAuth: String? = null
        var capturedKey: String? = null
        var capturedBody: String? = null
        val engine = MockEngine { request ->
            capturedPath = request.url.encodedPath
            capturedAuth = request.headers[HttpHeaders.Authorization]
            capturedKey = request.headers["X-Idempotency-Key"]
            capturedBody = (request.body as TextContent).text
            respond(
                content = """{"id":"pref-123","init_point":"https://checkout.mercadopago.com/pref-123"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        client(engine).use { client ->
            val preference = client.createPreference(
                internalPaymentId = "pay-1",
                purchaseId = "purchase-1",
                installmentId = "installment-1",
                amountCents = 18_750,
                description = "Cuota 1 - Distribuidora Lima",
                notificationUrl = "https://payments.example.com/v1/webhooks/mercadopago",
                backUrl = "https://payments.example.com/back"
            )
            assertEquals("pref-123", preference.id)
            assertEquals("https://checkout.mercadopago.com/pref-123", preference.initPoint)
        }

        assertEquals("/checkout/preferences", capturedPath)
        assertEquals("Bearer test-token", capturedAuth)
        assertEquals("pay-1", capturedKey)
        val body = capturedBody.orEmpty()
        assertTrue(body.contains("\"external_reference\":\"pay-1\""))
        assertTrue(body.contains("\"unit_price\":187.5"))
        assertTrue(body.contains("\"notification_url\":\"https://payments.example.com/v1/webhooks/mercadopago\""))
        assertTrue(body.contains("\"payment_id\":\"pay-1\""))
    }

    @Test
    fun `parses an authoritative mercadopago payment`() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("/v1/payments/12345", request.url.encodedPath)
            assertEquals("Bearer test-token", request.headers[HttpHeaders.Authorization])
            respond(
                content = """
                    {
                      "id": 12345,
                      "status": "approved",
                      "status_detail": "accredited",
                      "transaction_amount": 187.5,
                      "currency_id": "PEN",
                      "external_reference": "pay-1",
                      "metadata": {"payment_id": "pay-1"},
                      "payment_method_id": "visa",
                      "date_approved": "2026-10-20T12:00:00.000-05:00"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        client(engine).use { client ->
            val details = client.fetchPayment("12345")
            assertEquals("12345", details.providerPaymentId)
            assertEquals("approved", details.status)
            assertEquals("accredited", details.statusDetail)
            assertEquals(18_750L, details.amountCents)
            assertEquals("PEN", details.currency)
            assertEquals("pay-1", details.externalReference)
            assertEquals("pay-1", details.metadataPaymentId)
            assertEquals("visa", details.paymentMethodId)
            assertEquals("2026-10-20T17:00:00Z", details.occurredAt)
        }
    }

    @Test
    fun `throws a provider exception on non success responses`() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """{"message":"unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        client(engine).use { client ->
            val error = assertFailsWith<PaymentProviderException> { client.fetchPayment("12345") }
            assertEquals("MERCADO_PAGO_PAYMENT_LOOKUP_FAILED", error.code)
        }
    }

    private fun client(engine: MockEngine): MercadoPagoClient {
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        }
        return MercadoPagoClient("test-token", "https://api.test", httpClient)
    }
}
