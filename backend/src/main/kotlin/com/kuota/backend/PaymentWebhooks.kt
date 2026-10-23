package com.kuota.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

internal enum class ProviderPaymentStatus { APPROVED, PENDING, PROCESSING, REJECTED, REFUNDED, REVERSED }

internal data class ProviderSettlement(
    val internalPaymentId: String,
    val provider: String,
    val providerPaymentId: String,
    val status: ProviderPaymentStatus,
    val amountCents: Long?,
    val currency: String?,
    val occurredAt: String,
    val reasonCode: String
)

internal sealed interface SettlementResult {
    data class Applied(val paymentStatus: String, val reconciliationStatus: String) : SettlementResult
    data class Duplicate(val paymentStatus: String) : SettlementResult
    data object NotFound : SettlementResult
    data class Review(val reasonCode: String) : SettlementResult
    data class Rejected(val code: String, val message: String) : SettlementResult
}

internal interface PaymentSettlementRepository {
    fun settle(update: ProviderSettlement): SettlementResult
}

internal data class WebhookOutcome(
    val httpStatus: Int,
    val body: Map<String, String>
)

internal class PaymentWebhookProcessor(
    private val repository: PaymentSettlementRepository,
    private val accounting: AccountingStore,
    private val environment: String,
    private val mercadoPagoLookup: MercadoPagoPaymentLookup?,
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {
    private val logger = LoggerFactory.getLogger(PaymentWebhookProcessor::class.java)

    suspend fun processMercadoPago(rawBody: String, queryDataId: String?): WebhookOutcome {
        val lookup = mercadoPagoLookup
            ?: return outcome(503, "MERCADO_PAGO_NOT_CONFIGURED", "Mercado Pago webhooks are not configured.")
        val notification = runCatching { json.decodeFromString<MercadoPagoWebhookPayload>(rawBody) }.getOrNull()
            ?: return outcome(400, "WEBHOOK_PAYLOAD_INVALID", "The webhook payload is not valid JSON.")
        val bodyDataId = notification.data?.id?.let(::scalarContent)
        val dataId = bodyDataId ?: queryDataId
        if (dataId.isNullOrBlank()) {
            return outcome(400, "WEBHOOK_DATA_ID_MISSING", "The payment identifier is missing.")
        }
        if (!queryDataId.isNullOrBlank() && bodyDataId != null && queryDataId != bodyDataId) {
            return outcome(400, "WEBHOOK_DATA_ID_MISMATCH", "The signed payment identifier does not match the payload.")
        }
        if (!notification.type.isNullOrBlank() && notification.type != "payment") {
            return outcome(200, "IGNORED", "Only payment notifications are processed.")
        }
        val notificationId = notification.id?.let(::scalarContent)
            ?.takeIf { it.isNotBlank() && it.length <= 200 }
            ?: "body-${sha256Hex(rawBody.toByteArray(StandardCharsets.UTF_8)).take(32)}"
        val payloadHash = sha256Hex(rawBody.toByteArray(StandardCharsets.UTF_8))
        return when (val claim = accounting.beginWebhook("mercadopago", notificationId, environment, payloadHash)) {
            is WebhookClaim.Duplicate -> outcome(200, "DUPLICATE", "This notification was already processed.")
            is WebhookClaim.InProgress -> outcome(409, "WEBHOOK_IN_PROGRESS", "This notification is already being processed.")
            is WebhookClaim.Claimed -> {
                try {
                    val details = lookup.fetchPayment(dataId)
                    val internalPaymentId = details.externalReference ?: details.metadataPaymentId
                    if (internalPaymentId.isNullOrBlank()) {
                        finishIgnored(notificationId, "EXTERNAL_REFERENCE_MISSING")
                        return outcome(200, "IGNORED", "The payment does not reference a Kuota payment.")
                    }
                    val status = mapMercadoPagoStatus(details.status)
                    if (status == null) {
                        finishIgnored(notificationId, "UNSUPPORTED_PROVIDER_STATUS")
                        return outcome(200, "IGNORED", "The provider status is not supported.")
                    }
                    settleAndFinish(
                        provider = "mercadopago",
                        eventId = notificationId,
                        update = ProviderSettlement(
                            internalPaymentId = internalPaymentId,
                            provider = "mercadopago",
                            providerPaymentId = details.providerPaymentId,
                            status = status,
                            amountCents = details.amountCents,
                            currency = details.currency,
                            occurredAt = details.occurredAt,
                            reasonCode = "MERCADO_PAGO_${status.name}"
                        )
                    )
                } catch (error: PaymentProviderException) {
                    logger.warn("Mercado Pago lookup failed for event {}: {}", notificationId, error.code)
                    finishFailed(notificationId, error.code)
                    outcome(502, error.code, "No se pudo verificar el pago con Mercado Pago.")
                } catch (error: Exception) {
                    logger.error("Mercado Pago webhook processing failed for event {}", notificationId, error)
                    finishFailed(notificationId, "WEBHOOK_PROCESSING_FAILED")
                    outcome(500, "WEBHOOK_PROCESSING_FAILED", "No se pudo procesar la notificación de pago.")
                }
            }
        }
    }

    fun processHmac(rawBody: String): WebhookOutcome {
        val payload = runCatching { json.decodeFromString<NormalizedWebhookPayload>(rawBody) }.getOrNull()
            ?: return outcome(400, "WEBHOOK_PAYLOAD_INVALID", "The webhook payload is not valid JSON.")
        if (payload.eventId.isBlank() || payload.eventId.length > 200) {
            return outcome(400, "WEBHOOK_EVENT_ID_INVALID", "event_id is required and must not exceed 200 characters.")
        }
        if (payload.paymentId.isBlank()) {
            return outcome(400, "WEBHOOK_PAYMENT_ID_MISSING", "payment_id is required.")
        }
        val provider = payload.provider.trim().lowercase().ifBlank { "generic" }
        if (!PROVIDER_PATTERN.matches(provider)) {
            return outcome(400, "WEBHOOK_PROVIDER_INVALID", "provider contains unsupported characters.")
        }
        val status = mapNormalizedStatus(payload.status)
            ?: return outcome(400, "WEBHOOK_STATUS_INVALID", "status is not supported.")
        val currency = (payload.currency ?: CURRENCY_PEN).uppercase()
        if (!CURRENCY_PATTERN.matches(currency)) {
            return outcome(400, "WEBHOOK_CURRENCY_INVALID", "currency must be a three-letter code.")
        }
        val amount = payload.amountCents
        if (amount != null && amount <= 0) {
            return outcome(400, "WEBHOOK_AMOUNT_INVALID", "amount_cents must be positive when provided.")
        }
        val occurredAt = payload.occurredAt?.let { raw ->
            runCatching { Instant.parse(raw).toString() }.getOrNull()
                ?: return outcome(400, "WEBHOOK_TIMESTAMP_INVALID", "occurred_at must be an ISO-8601 instant.")
        } ?: Instant.now().toString()
        val payloadHash = sha256Hex(rawBody.toByteArray(StandardCharsets.UTF_8))

        return when (val claim = accounting.beginWebhook(provider, payload.eventId, environment, payloadHash)) {
            is WebhookClaim.Duplicate -> outcome(200, "DUPLICATE", "This event was already processed.")
            is WebhookClaim.InProgress -> outcome(409, "WEBHOOK_IN_PROGRESS", "This event is already being processed.")
            is WebhookClaim.Claimed -> settleAndFinish(
                provider = provider,
                eventId = payload.eventId,
                update = ProviderSettlement(
                    internalPaymentId = payload.paymentId,
                    provider = provider,
                    providerPaymentId = payload.providerPaymentId?.takeIf { it.isNotBlank() } ?: payload.eventId,
                    status = status,
                    amountCents = amount,
                    currency = currency,
                    occurredAt = occurredAt,
                    reasonCode = "HMAC_${status.name}"
                )
            )
        }
    }

    private fun settleAndFinish(provider: String, eventId: String, update: ProviderSettlement): WebhookOutcome {
        return try {
            val result = repository.settle(update)
            when (result) {
                is SettlementResult.Applied -> {
                    finish(provider, eventId, WEBHOOK_COMPLETED, result.paymentStatus)
                    outcome(200, "APPLIED", "Payment status recorded.")
                }
                is SettlementResult.Duplicate -> {
                    finish(provider, eventId, WEBHOOK_COMPLETED, "DUPLICATE")
                    outcome(200, "DUPLICATE", "Payment status was already recorded.")
                }
                is SettlementResult.Review -> {
                    finish(provider, eventId, WEBHOOK_COMPLETED, result.reasonCode)
                    outcome(200, "REVIEW", "Payment needs reconciliation review.")
                }
                is SettlementResult.Rejected -> {
                    finish(provider, eventId, WEBHOOK_COMPLETED, result.code)
                    outcome(200, "REJECTED", result.message)
                }
                SettlementResult.NotFound -> {
                    finish(provider, eventId, WEBHOOK_IGNORED, "PAYMENT_NOT_FOUND")
                    outcome(200, "IGNORED", "The Kuota payment was not found.")
                }
            }
        } catch (error: Exception) {
            logger.error("Settlement failed for {} event {}", provider, eventId, error)
            finishFailed(eventId, "SETTLEMENT_FAILED", provider)
            outcome(500, "WEBHOOK_SETTLEMENT_FAILED", "No se pudo registrar el pago.")
        }
    }

    private fun finishIgnored(eventId: String, note: String) {
        runCatching { accounting.finishWebhook("mercadopago", eventId, WEBHOOK_IGNORED, note) }
            .onFailure { logger.warn("Could not mark webhook {} as ignored", eventId, it) }
    }

    private fun finishFailed(eventId: String, note: String, provider: String = "mercadopago") {
        runCatching { accounting.finishWebhook(provider, eventId, WEBHOOK_FAILED, note) }
            .onFailure { logger.warn("Could not mark webhook {} as failed", eventId, it) }
    }

    private fun finish(provider: String, eventId: String, status: String, note: String) {
        accounting.finishWebhook(provider, eventId, status, note)
    }

    private fun mapMercadoPagoStatus(raw: String): ProviderPaymentStatus? = when (raw.lowercase()) {
        "approved" -> ProviderPaymentStatus.APPROVED
        "pending" -> ProviderPaymentStatus.PENDING
        "in_process", "authorized" -> ProviderPaymentStatus.PROCESSING
        "rejected", "cancelled" -> ProviderPaymentStatus.REJECTED
        "refunded" -> ProviderPaymentStatus.REFUNDED
        "charged_back" -> ProviderPaymentStatus.REVERSED
        else -> null
    }

    private fun mapNormalizedStatus(raw: String): ProviderPaymentStatus? =
        when (raw.trim().uppercase()) {
            "APPROVED", "SUCCEEDED", "PAID" -> ProviderPaymentStatus.APPROVED
            "PENDING" -> ProviderPaymentStatus.PENDING
            "PROCESSING", "IN_PROCESS" -> ProviderPaymentStatus.PROCESSING
            "REJECTED", "FAILED", "CANCELLED" -> ProviderPaymentStatus.REJECTED
            "REFUNDED" -> ProviderPaymentStatus.REFUNDED
            "REVERSED", "CHARGED_BACK" -> ProviderPaymentStatus.REVERSED
            else -> null
        }

    private fun outcome(status: Int, code: String, message: String): WebhookOutcome =
        WebhookOutcome(status, mapOf("code" to code, "message" to message))

    private fun scalarContent(element: JsonElement): String? =
        runCatching { element.jsonPrimitive.contentOrNull }.getOrNull()

    private companion object {
        val PROVIDER_PATTERN = Regex("[a-z0-9_-]{1,32}")
        val CURRENCY_PATTERN = Regex("[A-Z]{3}")
    }
}

internal class MercadoPagoSignatureVerifier(
    private val secret: String,
    private val toleranceSeconds: Long = 300,
    private val clock: () -> Instant = Instant::now
) {
    fun verify(signatureHeader: String?, requestId: String?, dataId: String?): Boolean {
        if (secret.isBlank() || signatureHeader.isNullOrBlank() || dataId.isNullOrBlank()) return false
        val parts = signatureHeader.split(',')
            .mapNotNull { part ->
                val pieces = part.trim().split('=', limit = 2)
                if (pieces.size == 2) pieces[0].trim() to pieces[1].trim() else null
            }
            .toMap()
        val timestamp = parts["ts"]?.toLongOrNull() ?: return false
        val provided = parts["v1"]?.lowercase() ?: return false
        if (provided.length != 64) return false
        if (abs(clock().epochSecond - timestamp) > toleranceSeconds) return false
        val manifest = buildString {
            append("id:").append(dataId).append(';')
            if (!requestId.isNullOrBlank()) append("request-id:").append(requestId).append(';')
            append("ts:").append(timestamp).append(';')
        }
        val expected = hmacSha256Hex(secret, manifest.toByteArray(StandardCharsets.UTF_8))
        return MessageDigest.isEqual(expected.toByteArray(StandardCharsets.UTF_8), provided.toByteArray(StandardCharsets.UTF_8))
    }
}

internal object HmacWebhookVerifier {
    fun sign(secret: String, timestamp: Long, body: ByteArray): String =
        "v1=" + hmacSha256Hex(secret, "$timestamp.".toByteArray(StandardCharsets.UTF_8) + body)

    fun verify(
        secret: String?,
        timestampHeader: String?,
        signatureHeader: String?,
        body: ByteArray,
        toleranceSeconds: Long = 300,
        now: Instant = Instant.now()
    ): Boolean {
        if (secret.isNullOrBlank() || timestampHeader.isNullOrBlank() || signatureHeader.isNullOrBlank()) return false
        val timestamp = timestampHeader.toLongOrNull() ?: return false
        if (abs(now.epochSecond - timestamp) > toleranceSeconds) return false
        val provided = signatureHeader.removePrefix("v1=").trim().lowercase()
        if (provided.length != 64) return false
        val expected = hmacSha256Hex(secret, "$timestamp.".toByteArray(StandardCharsets.UTF_8) + body)
        return MessageDigest.isEqual(expected.toByteArray(StandardCharsets.UTF_8), provided.toByteArray(StandardCharsets.UTF_8))
    }
}

internal fun hmacSha256Hex(secret: String, message: ByteArray): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
    return mac.doFinal(message).toHex()
}

internal fun sha256Hex(message: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(message).toHex()

private fun ByteArray.toHex(): String {
    val hex = "0123456789abcdef"
    val result = StringBuilder(size * 2)
    forEach { byte ->
        val value = byte.toInt() and 0xff
        result.append(hex[value ushr 4])
        result.append(hex[value and 0x0f])
    }
    return result.toString()
}

@Serializable
internal data class NormalizedWebhookPayload(
    @SerialName("event_id") val eventId: String,
    val provider: String = "generic",
    @SerialName("payment_id") val paymentId: String,
    @SerialName("provider_payment_id") val providerPaymentId: String? = null,
    val status: String,
    @SerialName("amount_cents") val amountCents: Long? = null,
    val currency: String? = CURRENCY_PEN,
    @SerialName("occurred_at") val occurredAt: String? = null
)

@Serializable
private data class MercadoPagoWebhookPayload(
    val id: JsonElement? = null,
    val type: String? = null,
    val action: String? = null,
    val data: MercadoPagoWebhookData? = null
)

@Serializable
private data class MercadoPagoWebhookData(
    val id: JsonElement? = null
)
