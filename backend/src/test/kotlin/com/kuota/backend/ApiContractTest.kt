package com.kuota.backend

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiContractTest {

    private lateinit var directory: Path
    private lateinit var accounting: FileAccountingStore
    private lateinit var store: DemoStore
    private lateinit var config: BackendConfig
    private lateinit var services: KuotaServices
    private lateinit var webhookProcessor: PaymentWebhookProcessor
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("kuota-api-contract")
        config = BackendConfig(
            environment = RuntimeEnvironment.DEMO,
            paymentProvider = PaymentProviderKind.DEMO,
            port = 8080,
            host = "127.0.0.1",
            demoStorePath = directory.resolve("kuota-demo-store.json"),
            database = null,
            webhookHmacSecret = "test-webhook-secret-0123456789abcdef",
            adminToken = null,
            mercadoPago = null,
            jwtSecret = "test-jwt-secret-0123456789abcdef0123456789",
            qrSigningSecret = "test-qr-secret-0123456789abcdef0123456789"
        )
        JwtConfig.initialize(config.jwtSecret)
        DynamicQrSigner.initialize(config.qrSigningSecret)
        accounting = FileAccountingStore(
            ledgerFile = directory.resolve("kuota-ledger.ndjson"),
            webhookFile = directory.resolve("kuota-webhook-events.ndjson")
        )
        store = DemoStore(
            file = directory.resolve("kuota-demo-store.json"),
            paymentProvider = DemoPaymentProviderAdapter,
            accounting = accounting,
            environment = "DEMO"
        )
        webhookProcessor = PaymentWebhookProcessor(store, accounting, "DEMO", null)
        val claims = ClaimStore(directory.resolve("kuota-claims.json")) { emptyList() }
        val forwarding = ClaimForwardingService(claims, emptyList())
        val devices = DeviceStore(directory.resolve("kuota-devices.json"))
        val push = PushService(devices, DryRunPushSender)
        val reminders = ReminderScheduler(devices, push) { emptyList() }
        val declaredSales = DeclaredSalesStore(directory.resolve("kuota-declared-sales.json"))
        services = KuotaServices(claims, forwarding, devices, push, reminders, declaredSales)
    }

    @AfterTest
    fun tearDown() {
        accounting.close()
    }

    private fun apiTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            kuotaModule(store, services, config, accounting, DemoPaymentProviderAdapter, webhookProcessor)
        }
        block()
    }

    @Test
    fun `responses always carry a safe request id`() = apiTest {
        val echoed = client.get("/health") { header("X-Request-Id", "client-request-abc") }
        assertEquals(HttpStatusCode.OK, echoed.status)
        assertEquals("client-request-abc", echoed.headers["X-Request-Id"])
        assertEquals("nosniff", echoed.headers["X-Content-Type-Options"])
        assertEquals("no-referrer", echoed.headers["Referrer-Policy"])

        val replaced = client.get("/health") { header("X-Request-Id", "bad id with spaces") }
        val generated = replaced.headers["X-Request-Id"]
        assertNotNull(generated)
        assertNotEquals("bad id with spaces", generated)
        assertTrue(generated.matches(Regex("^[0-9a-fA-F-]{36}$")))
    }

    @Test
    fun `errors expose request id and retryable flags`() = apiTest {
        val wrongOtp = client.postJson(
            "/v1/auth/verify-otp",
            """{"phone":"987654321","otp_code":"000000"}""",
            requestId = "req-error-001"
        )
        assertEquals(HttpStatusCode.Unauthorized, wrongOtp.status)
        val otpError = json.parseToJsonElement(wrongOtp.bodyAsText()).jsonObject
        assertEquals("OTP_INVALID", otpError.string("code"))
        assertEquals("req-error-001", otpError.string("request_id"))
        assertEquals(false, otpError.boolean("retryable"))

        val unauthenticated = client.get("/v1/purchases") { header("X-Request-Id", "req-auth-001") }
        assertEquals(HttpStatusCode.Unauthorized, unauthenticated.status)
        val authError = json.parseToJsonElement(unauthenticated.bodyAsText()).jsonObject
        assertEquals("AUTH_TOKEN_INVALID", authError.string("code"))
        assertEquals("req-auth-001", authError.string("request_id"))
        assertEquals(false, authError.boolean("retryable"))
    }

    @Test
    fun `signed supplier qr verifies and rejects tampered amounts`() = apiTest {
        val signedPayload = client.generateQr("100.50")
        val verifiedResponse = client.postJson(
            "/v1/supplier/qr/verify",
            buildJsonObject { put("qr_payload", signedPayload) }.toString()
        )
        assertEquals(HttpStatusCode.OK, verifiedResponse.status, verifiedResponse.bodyAsText())
        val verified = json.parseToJsonElement(verifiedResponse.bodyAsText()).jsonObject
        assertEquals(10_050L, verified.long("amount_cents"))
        assertEquals("Distribuidora Lima S.A.C.", verified.string("supplier_name"))
        assertEquals(true, verified.boolean("demo_only"))

        val decoded = String(Base64.getUrlDecoder().decode(signedPayload), Charsets.UTF_8)
        val tamperedJson = buildJsonObject {
            json.parseToJsonElement(decoded).jsonObject.forEach { (key, value) -> put(key, value) }
            put("amount_cents", 1L)
        }
        val tamperedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(tamperedJson.toString().toByteArray(Charsets.UTF_8))

        val rejectedResponse = client.postJson(
            "/v1/supplier/qr/verify",
            buildJsonObject { put("qr_payload", tamperedPayload) }.toString()
        )
        assertEquals(HttpStatusCode.UnprocessableEntity, rejectedResponse.status)
        assertEquals(
            "QR_INVALID_OR_EXPIRED",
            json.parseToJsonElement(rejectedResponse.bodyAsText()).jsonObject.string("code")
        )
    }

    @Test
    fun `otp only accepts the documented demo code`() = apiTest {
        val requested = client.postJson("/v1/auth/request-otp", """{"phone":"987654321"}""")
        assertEquals(HttpStatusCode.OK, requested.status)
        assertEquals(
            "123456",
            json.parseToJsonElement(requested.bodyAsText()).jsonObject.string("otp_code_debug")
        )

        val wrong = client.postJson("/v1/auth/verify-otp", """{"phone":"987654321","otp_code":"654321"}""")
        assertEquals(HttpStatusCode.Unauthorized, wrong.status)
        assertEquals("OTP_INVALID", json.parseToJsonElement(wrong.bodyAsText()).jsonObject.string("code"))

        val token = client.authenticate("987654321")
        val profile = client.get("/v1/me") { header(HttpHeaders.Authorization, "Bearer $token") }
        assertEquals(HttpStatusCode.OK, profile.status)
        assertEquals("user-987654321", json.parseToJsonElement(profile.bodyAsText()).jsonObject.string("user_id"))
    }

    @Test
    fun `purchase history is scoped to the authenticated owner`() = apiTest {
        val ownerToken = client.authenticate("987654321")
        val otherToken = client.authenticate("912345678")

        val signedPayload = client.generateQr("100.50")
        val decisionId = client.eligibility(10_050L)
        val quote = client.quote(10_050L, signedPayload, decisionId)

        val created = client.purchase(ownerToken, quote)
        assertEquals(HttpStatusCode.Created, created.status, created.bodyAsText())
        val createdBody = json.parseToJsonElement(created.bodyAsText()).jsonObject
        assertEquals("DEMO_CREATED", createdBody.string("status"))
        val purchaseId = createdBody.string("purchase_id")

        val ownerHistory = client.get("/v1/purchases") { header(HttpHeaders.Authorization, "Bearer $ownerToken") }
        assertEquals(HttpStatusCode.OK, ownerHistory.status)
        val ownerItems = json.parseToJsonElement(ownerHistory.bodyAsText()).jsonArray
        assertEquals(1, ownerItems.size)
        assertEquals(purchaseId, ownerItems.single().jsonObject.string("purchase_id"))

        val otherHistory = client.get("/v1/purchases") { header(HttpHeaders.Authorization, "Bearer $otherToken") }
        assertEquals(HttpStatusCode.OK, otherHistory.status)
        assertTrue(json.parseToJsonElement(otherHistory.bodyAsText()).jsonArray.isEmpty())

        val otherDetail = client.get("/v1/purchases/$purchaseId") {
            header(HttpHeaders.Authorization, "Bearer $otherToken")
        }
        assertEquals(HttpStatusCode.NotFound, otherDetail.status)
        assertEquals("PURCHASE_NOT_FOUND", json.parseToJsonElement(otherDetail.bodyAsText()).jsonObject.string("code"))
    }

    @Test
    fun `quoting rejects a request amount that differs from the signed qr`() = apiTest {
        val signedPayload = client.generateQr("100.50")
        val decisionId = client.eligibility(20_000L)

        val mismatched = client.postJson(
            "/v1/quotes",
            buildJsonObject {
                put("business_id", "demo-buyer-001")
                put("supplier_qr_payload", signedPayload)
                put("amount_cents", 20_000L)
                put("term_days", 30)
                put("eligibility_decision_id", decisionId)
            }.toString()
        )
        assertEquals(HttpStatusCode.UnprocessableEntity, mismatched.status)
        assertEquals(
            "QR_AMOUNT_MISMATCH",
            json.parseToJsonElement(mismatched.bodyAsText()).jsonObject.string("code")
        )
    }

    @Test
    fun `supplier qr generation is rate limited with retry information`() = apiTest {
        repeat(10) {
            val response = client.post("/v1/supplier/generate-qr") {
                setBody(FormDataContent(Parameters.build { append("amountPen", "1.00") }))
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }

        val limited = client.post("/v1/supplier/generate-qr") {
            setBody(FormDataContent(Parameters.build { append("amountPen", "1.00") }))
        }
        assertEquals(HttpStatusCode.TooManyRequests, limited.status)
        assertNotNull(limited.headers[HttpHeaders.RetryAfter])
        val error = json.parseToJsonElement(limited.bodyAsText()).jsonObject
        assertEquals("RATE_LIMITED", error.string("code"))
        assertEquals(true, error.boolean("retryable"))
    }

    @Test
    fun `versioned eligibility policy separates risk score from down payment and scales down to zero`() = apiTest {
        val entryResponse = client.postJson(
            "/v1/eligibility/checks",
            buildJsonObject {
                put("business_id", "demo-buyer-001")
                put("amount_cents", 100_000L)
                put("term_days", 30)
                put("consent_version", "demo-consent-1")
            }.toString()
        )
        assertEquals(HttpStatusCode.OK, entryResponse.status)
        val entry = json.parseToJsonElement(entryResponse.bodyAsText()).jsonObject
        assertEquals("DEMO_POLICY_2026_V2", entry.string("policy_version"))
        assertEquals(620L, entry.long("risk_score"))
        assertEquals("ENTRY_30", entry.string("risk_tier"))
        assertEquals(3_000L, entry.long("down_payment_basis_points"))

        val trustedResponse = client.postJson(
            "/v1/eligibility/checks",
            buildJsonObject {
                put("business_id", "demo-buyer-001")
                put("amount_cents", 100_000L)
                put("term_days", 30)
                put("consent_version", "demo-consent-1")
                put("simulated_paid_installments", 3)
            }.toString()
        )
        assertEquals(HttpStatusCode.OK, trustedResponse.status)
        val trusted = json.parseToJsonElement(trustedResponse.bodyAsText()).jsonObject
        assertEquals("DEMO_POLICY_2026_V2", trusted.string("policy_version"))
        assertEquals(860L, trusted.long("risk_score"))
        assertEquals("TRUSTED_0", trusted.string("risk_tier"))
        assertEquals(0L, trusted.long("down_payment_basis_points"))

        val signedPayload = client.generateQr("1000.00")
        val zeroDownQuote = client.quote(100_000L, signedPayload, trusted.string("decision_id"))
        assertEquals(0L, zeroDownQuote.long("buyer_down_payment_cents"))
        assertEquals(60_000L, zeroDownQuote.long("kouta_initial_contribution_cents"))
        assertEquals(60_000L, zeroDownQuote.long("supplier_immediate_payout_cents"))
        assertEquals(40_000L, zeroDownQuote.long("supplier_deferred_payout_cents"))
        assertEquals(105_000L, zeroDownQuote.long("buyer_total_after_down_payment_cents"))

        val token = client.authenticate("987654321")
        val created = client.purchase(token, zeroDownQuote)
        assertEquals(HttpStatusCode.Created, created.status)

        val settlementsResponse = client.get("/v1/supplier/settlements")
        assertEquals(HttpStatusCode.OK, settlementsResponse.status)
        val settlements = json.parseToJsonElement(settlementsResponse.bodyAsText()).jsonArray
        assertEquals(1, settlements.size)
        val settlement = settlements.single().jsonObject
        assertEquals("DEMO_IMMEDIATE_60_SCHEDULED", settlement.string("settlement_status"))
        assertEquals(60_000L, settlement.long("immediate_payout_cents"))
        assertEquals(36_000L, settlement.long("deferred_net_cents"))
    }

    private suspend fun HttpClient.postJson(
        path: String,
        body: String,
        requestId: String? = null,
        token: String? = null,
        idempotencyKey: String? = null
    ): HttpResponse = post(path) {
        contentType(ContentType.Application.Json)
        setBody(body)
        requestId?.let { header("X-Request-Id", it) }
        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        idempotencyKey?.let { header("Idempotency-Key", it) }
    }

    private suspend fun HttpClient.generateQr(amountPen: String): String {
        val response = post("/v1/supplier/generate-qr") {
            setBody(FormDataContent(Parameters.build { append("amountPen", amountPen) }))
        }
        assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        return json.parseToJsonElement(response.bodyAsText()).jsonObject.string("signed_qr_payload")
    }

    private suspend fun HttpClient.authenticate(phone: String): String {
        val response = postJson(
            "/v1/auth/verify-otp",
            """{"phone":"$phone","otp_code":"123456"}"""
        )
        assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        return json.parseToJsonElement(response.bodyAsText()).jsonObject.string("access_token")
    }

    private suspend fun HttpClient.eligibility(amountCents: Long): String {
        val response = postJson(
            "/v1/eligibility/checks",
            buildJsonObject {
                put("business_id", "demo-buyer-001")
                put("amount_cents", amountCents)
                put("term_days", 30)
                put("consent_version", "demo-consent-1")
            }.toString()
        )
        assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        return json.parseToJsonElement(response.bodyAsText()).jsonObject.string("decision_id")
    }

    private suspend fun HttpClient.quote(amountCents: Long, signedPayload: String, decisionId: String): JsonObject {
        val response = postJson(
            "/v1/quotes",
            buildJsonObject {
                put("business_id", "demo-buyer-001")
                put("supplier_qr_payload", signedPayload)
                put("amount_cents", amountCents)
                put("term_days", 30)
                put("eligibility_decision_id", decisionId)
            }.toString()
        )
        assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        return json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    private suspend fun HttpClient.purchase(token: String, quote: JsonObject): HttpResponse = postJson(
        "/v1/purchases",
        buildJsonObject {
            put("quote_id", quote.string("quote_id"))
            put("quote_version", quote.string("quote_version"))
            put("accepted_terms_version", "DEMO_ONLY_NOT_ACCEPTED")
            put("accepted_privacy_notice_version", "DEMO_ONLY_NOT_ACCEPTED")
        }.toString(),
        token = token,
        idempotencyKey = "test-purchase-${UUID.randomUUID()}"
    )
}

private fun JsonObject.string(key: String): String =
    assertNotNull((this[key] as? JsonPrimitive)?.contentOrNull, "Missing JSON string field '$key'")

private fun JsonObject.long(key: String): Long =
    assertNotNull((this[key] as? JsonPrimitive)?.contentOrNull?.toLongOrNull(), "Missing JSON number field '$key'")

private fun JsonObject.boolean(key: String): Boolean =
    assertNotNull((this[key] as? JsonPrimitive)?.booleanOrNull, "Missing JSON boolean field '$key'")
