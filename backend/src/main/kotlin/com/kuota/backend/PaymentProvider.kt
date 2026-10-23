package com.kuota.backend

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.math.RoundingMode
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

internal data class PaymentCheckoutRequest(
    val internalPaymentId: String,
    val purchaseId: String,
    val installmentId: String,
    val amountCents: Long,
    val description: String
)

internal data class PaymentCheckout(
    val checkoutReference: String?,
    val nextActionUrl: String?,
    val status: String,
    val reasonCode: String
)

internal interface PaymentProviderAdapter : AutoCloseable {
    val id: String
    val paymentMethodId: String
    val isReal: Boolean
    fun newPaymentId(): String
    suspend fun createCheckout(request: PaymentCheckoutRequest): PaymentCheckout
    override fun close() = Unit
}

internal object DemoPaymentProviderAdapter : PaymentProviderAdapter {
    override val id: String = "demo"
    override val paymentMethodId: String = "DEMO_MANUAL"
    override val isReal: Boolean = false
    override fun newPaymentId(): String = "demo-payment-${UUID.randomUUID()}"
    override suspend fun createCheckout(request: PaymentCheckoutRequest): PaymentCheckout =
        PaymentCheckout(
            checkoutReference = null,
            nextActionUrl = null,
            status = "PENDING",
            reasonCode = "DEMO_INTENT_CREATED"
        )
}

internal data class ProviderPaymentDetails(
    val providerPaymentId: String,
    val status: String,
    val statusDetail: String?,
    val amountCents: Long?,
    val currency: String?,
    val externalReference: String?,
    val metadataPaymentId: String?,
    val paymentMethodId: String?,
    val occurredAt: String
)

internal fun interface MercadoPagoPaymentLookup {
    suspend fun fetchPayment(providerPaymentId: String): ProviderPaymentDetails
}

internal class MercadoPagoProvider(
    private val config: MercadoPagoConfig,
    private val client: MercadoPagoClient = MercadoPagoClient(config.accessToken, config.apiBaseUrl)
) : PaymentProviderAdapter, MercadoPagoPaymentLookup {
    override val id: String = "mercadopago"
    override val paymentMethodId: String = "MERCADO_PAGO_CHECKOUT"
    override val isReal: Boolean = true

    override fun newPaymentId(): String = "kuota-payment-${UUID.randomUUID()}"

    override suspend fun createCheckout(request: PaymentCheckoutRequest): PaymentCheckout {
        val preference = client.createPreference(
            internalPaymentId = request.internalPaymentId,
            purchaseId = request.purchaseId,
            installmentId = request.installmentId,
            amountCents = request.amountCents,
            description = request.description,
            notificationUrl = config.notificationUrl,
            backUrl = config.backUrl
        )
        return PaymentCheckout(
            checkoutReference = preference.id,
            nextActionUrl = preference.initPoint,
            status = "ACTION_REQUIRED",
            reasonCode = "MERCADO_PAGO_CHECKOUT_CREATED"
        )
    }

    override suspend fun fetchPayment(providerPaymentId: String): ProviderPaymentDetails =
        client.fetchPayment(providerPaymentId)

    override fun close() = client.close()
}

internal class MercadoPagoClient(
    private val accessToken: String,
    private val apiBaseUrl: String = "https://api.mercadopago.com",
    private val httpClient: HttpClient = defaultHttpClient()
) : AutoCloseable {
    private val baseUrl = apiBaseUrl.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun createPreference(
        internalPaymentId: String,
        purchaseId: String,
        installmentId: String,
        amountCents: Long,
        description: String,
        notificationUrl: String,
        backUrl: String?
    ): MercadoPagoPreference {
        require(amountCents > 0) { "Checkout amount must be positive." }
        val body = MercadoPagoPreferenceRequest(
            items = listOf(
                MercadoPagoItem(
                    id = installmentId,
                    title = description.take(256),
                    quantity = 1,
                    currencyId = CURRENCY_PEN,
                    unitPrice = amountCents / 100.0
                )
            ),
            externalReference = internalPaymentId,
            notificationUrl = notificationUrl,
            backUrls = backUrl?.let { MercadoPagoBackUrls(success = it, failure = it, pending = it) },
            autoReturn = if (backUrl != null) "approved" else null,
            metadata = mapOf(
                "payment_id" to internalPaymentId,
                "purchase_id" to purchaseId,
                "installment_id" to installmentId
            )
        )
        val response = httpClient.post("$baseUrl/checkout/preferences") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header("X-Idempotency-Key", internalPaymentId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, "MERCADO_PAGO_PREFERENCE_FAILED", "No se pudo crear el checkout de Mercado Pago.")
        val parsed = response.body<MercadoPagoPreferenceResponse>()
        val checkoutUrl = parsed.initPoint ?: parsed.sandboxInitPoint
            ?: throw PaymentProviderException("MERCADO_PAGO_CHECKOUT_URL_MISSING", "Mercado Pago no devolvió una URL de checkout.")
        return MercadoPagoPreference(id = parsed.id, initPoint = checkoutUrl)
    }

    suspend fun fetchPayment(providerPaymentId: String): ProviderPaymentDetails {
        val response = httpClient.get("$baseUrl/v1/payments/$providerPaymentId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        ensureSuccess(response, "MERCADO_PAGO_PAYMENT_LOOKUP_FAILED", "No se pudo consultar el pago en Mercado Pago.")
        val body = response.body<JsonElement>().jsonObject
        val metadata = body["metadata"] as? JsonObject
        return ProviderPaymentDetails(
            providerPaymentId = body.string("id") ?: providerPaymentId,
            status = body.string("status") ?: "unknown",
            statusDetail = body.string("status_detail"),
            amountCents = body.string("transaction_amount")?.toBigDecimalOrNull()?.let { amount ->
                runCatching { amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact() }.getOrNull()
            },
            currency = body.string("currency_id"),
            externalReference = body.string("external_reference"),
            metadataPaymentId = metadata?.get("payment_id")?.let { runCatching { it.jsonPrimitive.contentOrNull }.getOrNull() },
            paymentMethodId = body.string("payment_method_id"),
            occurredAt = normalizeTimestamp(body.string("date_approved") ?: body.string("date_created"))
        )
    }

    override fun close() = httpClient.close()

    private fun ensureSuccess(response: HttpResponse, code: String, message: String) {
        if (!response.status.isSuccess()) {
            throw PaymentProviderException(code, "$message HTTP ${response.status.value}.")
        }
    }

    private fun JsonObject.string(name: String): String? =
        this[name]?.let { element -> runCatching { element.jsonPrimitive.contentOrNull }.getOrNull() }

    private fun normalizeTimestamp(raw: String?): String {
        if (raw.isNullOrBlank()) return Instant.now().toString()
        return runCatching { Instant.parse(raw) }
            .recoverCatching { OffsetDateTime.parse(raw).toInstant() }
            .getOrNull()
            ?.toString()
            ?: Instant.now().toString()
    }

    private companion object {
        fun defaultHttpClient(): HttpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
            expectSuccess = false
        }
    }
}

internal class PaymentProviderException(
    val code: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

internal fun createPaymentProvider(config: BackendConfig): PaymentProviderAdapter = when (config.paymentProvider) {
    PaymentProviderKind.DEMO -> DemoPaymentProviderAdapter
    PaymentProviderKind.MERCADO_PAGO -> MercadoPagoProvider(
        config = config.mercadoPago
            ?: error("Mercado Pago configuration is missing.")
    )
}

internal data class MercadoPagoPreference(
    val id: String,
    val initPoint: String
)

@Serializable
private data class MercadoPagoPreferenceRequest(
    val items: List<MercadoPagoItem>,
    @SerialName("external_reference") val externalReference: String,
    @SerialName("notification_url") val notificationUrl: String,
    @SerialName("back_urls") val backUrls: MercadoPagoBackUrls? = null,
    @SerialName("auto_return") val autoReturn: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
private data class MercadoPagoItem(
    val id: String,
    val title: String,
    val quantity: Int,
    @SerialName("currency_id") val currencyId: String,
    @SerialName("unit_price") val unitPrice: Double
)

@Serializable
private data class MercadoPagoBackUrls(
    val success: String,
    val failure: String,
    val pending: String
)

@Serializable
private data class MercadoPagoPreferenceResponse(
    val id: String,
    @SerialName("init_point") val initPoint: String? = null,
    @SerialName("sandbox_init_point") val sandboxInitPoint: String? = null
)
