package com.kuota.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.time.Clock
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val QR_SIGNING_MIN_SECRET_LENGTH = 32
internal const val QR_MAX_PAYLOAD_CHARS = 4096

@Serializable
data class DynamicQrPayload(
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("expires_at_epoch_millis") val expiresAtEpochMillis: Long,
    val signature: String = ""
)

internal class DynamicQrSigningService(
    private val secret: String,
    private val clock: Clock = Clock.systemUTC()
) {
    init {
        require(secret.length >= QR_SIGNING_MIN_SECRET_LENGTH) {
            "QR signing secret must have at least $QR_SIGNING_MIN_SECRET_LENGTH characters."
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun generateSignedQrPayload(
        transactionId: String,
        supplierId: String,
        supplierName: String,
        amountCents: Long,
        validityMinutes: Long = 5
    ): String {
        require(transactionId.isNotBlank()) { "transactionId cannot be blank." }
        require(supplierId.isNotBlank()) { "supplierId cannot be blank." }
        require(supplierName.isNotBlank()) { "supplierName cannot be blank." }
        require(amountCents > 0) { "amountCents must be positive." }
        require(validityMinutes in 1..1_440) { "validityMinutes must be between 1 and 1440." }

        val expiresAt = clock.millis() + (validityMinutes * 60 * 1000)
        val signature = signContent(contentToSign(transactionId, supplierId, supplierName, amountCents, expiresAt))

        val payload = DynamicQrPayload(
            transactionId = transactionId,
            supplierId = supplierId,
            supplierName = supplierName,
            amountCents = amountCents,
            expiresAtEpochMillis = expiresAt,
            signature = signature
        )

        val payloadJson = json.encodeToString(payload)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray(Charsets.UTF_8))
    }

    fun verifyQrPayload(base64Payload: String): DynamicQrPayload? {
        if (base64Payload.isBlank() || base64Payload.length > QR_MAX_PAYLOAD_CHARS) return null
        return try {
            val jsonBytes = Base64.getUrlDecoder().decode(base64Payload.trim())
            val payload = json.decodeFromString<DynamicQrPayload>(String(jsonBytes, Charsets.UTF_8))

            if (payload.transactionId.isBlank() || payload.supplierId.isBlank() ||
                payload.supplierName.isBlank() || payload.amountCents <= 0
            ) {
                return null
            }
            if (clock.millis() > payload.expiresAtEpochMillis) return null

            val expectedSignature = signContent(
                contentToSign(
                    payload.transactionId,
                    payload.supplierId,
                    payload.supplierName,
                    payload.amountCents,
                    payload.expiresAtEpochMillis
                )
            )
            val providedSignature = runCatching {
                Base64.getUrlDecoder().decode(payload.signature)
            }.getOrNull() ?: return null
            val expectedBytes = Base64.getUrlDecoder().decode(expectedSignature)

            if (!MessageDigest.isEqual(expectedBytes, providedSignature)) return null

            payload
        } catch (_: Exception) {
            null
        }
    }

    private fun contentToSign(
        transactionId: String,
        supplierId: String,
        supplierName: String,
        amountCents: Long,
        expiresAtEpochMillis: Long
    ): String = "$transactionId|$supplierId|$supplierName|$amountCents|$expiresAtEpochMillis"

    private fun signContent(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}

internal object DynamicQrSigner {
    @Volatile
    private var service: DynamicQrSigningService? = null

    fun initialize(secret: String) {
        service = DynamicQrSigningService(secret)
    }

    fun generateSignedQrPayload(
        transactionId: String,
        supplierId: String,
        supplierName: String,
        amountCents: Long,
        validityMinutes: Long = 5
    ): String = (service ?: error("QR signing service is not initialized."))
        .generateSignedQrPayload(transactionId, supplierId, supplierName, amountCents, validityMinutes)

    fun verifyQrPayload(base64Payload: String): DynamicQrPayload? =
        service?.verifyQrPayload(base64Payload)
}
